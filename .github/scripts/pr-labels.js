const CONVENTIONAL_TITLE = /^(\w+)(?:\(([^)]*)\))?(!)?:\s/;
const TITLE_TYPES = {
  feat: 'feat', fix: 'fix', refactor: 'refactor', perf: 'perf', docs: 'docs',
  test: 'test', ci: 'ci', build: 'ci', chore: 'chore', style: 'style',
};
/** chore(docs) and chore(ci) are how this repo commits docs and CI changes */
const CHORE_SCOPES = ['docs', 'ci'];
const TEMPLATE_TYPE = /^- \[x\] (bugfix|feature|breaking-change|refactor|performance|chore|ci|docs|test|style)\s*$/im;
const TEMPLATE_TYPES = {
  bugfix: 'fix', feature: 'feat', 'breaking-change': 'breaking', refactor: 'refactor', performance: 'perf',
  chore: 'chore', ci: 'ci', docs: 'docs', test: 'test', style: 'style',
};
const BREAKING_FOOTER = /^BREAKING[ -]CHANGE:/m;
const CLOSING_REF = /\b(?:close[sd]?|fix(?:e[sd])?|resolve[sd]?):?\s+#(\d+)\b/gi;

function typeKeys({ title, body, base, head }) {
  const keys = new Set();
  const conventional = title.match(CONVENTIONAL_TITLE);
  if (conventional) {
    const [, type, scope, bang] = conventional;
    const key = TITLE_TYPES[type.toLowerCase()];
    if (key === 'chore' && CHORE_SCOPES.includes(scope)) keys.add(scope);
    else if (key) keys.add(key);
    if (bang) keys.add('breaking');
  } else {
    const checked = body.match(TEMPLATE_TYPE);
    if (checked) keys.add(TEMPLATE_TYPES[checked[1].toLowerCase()]);
  }
  if (BREAKING_FOOTER.test(body)) keys.add('breaking');
  if (base === 'master' && head === 'develop') keys.add('release');
  return keys;
}

function closingRefs(body) {
  return new Set([...body.matchAll(CLOSING_REF)].map(match => Number(match[1])));
}

function minus(a, b) {
  return [...a].filter(item => !b.has(item));
}

module.exports = async ({ github, context }) => {
  const labels = require('./labels.js')(github, context);
  const { definitions } = labels;
  const { action, pull_request: pr, changes = {} } = context.payload;
  const current = new Set(pr.labels.map(label => label.name));
  const sameRepo = pr.head.repo?.full_name === pr.base.repo.full_name;

  const after = { title: pr.title, body: pr.body || '', base: pr.base.ref, head: sameRepo ? pr.head.ref : '' };
  // edited payloads only carry the fields that changed
  const before = {
    ...after,
    title: changes.title?.from ?? after.title,
    body: changes.body?.from ?? after.body,
    base: changes.base?.ref?.from ?? after.base,
  };
  const names = keys => new Set([...keys].map(key => definitions[key].name));
  const wanted = names(typeKeys(after));

  async function markIssues(numbers) {
    for (const number of numbers) {
      const { data: issue } = await github.rest.issues.get({ ...context.repo, issue_number: number }).catch(() => ({}));
      if (issue && !issue.pull_request && issue.state === 'open') {
        await labels.add(number, [definitions.inProgress.name]);
      }
    }
  }

  async function unmarkIssues(numbers) {
    for (const number of numbers) await labels.remove(number, [definitions.inProgress.name]);
  }

  switch (action) {
    case 'opened':
    case 'reopened':
      if (pr.draft) wanted.add(definitions.wip.name);
      await labels.add(pr.number, minus(wanted, current));
      await markIssues(closingRefs(after.body));
      break;

    case 'edited': {
      // Only touch labels whose source text changed, so labels added or removed by hand stay
      const previous = names(typeKeys(before));
      await labels.remove(pr.number, minus(previous, wanted).filter(name => current.has(name)));
      await labels.add(pr.number, minus(wanted, previous).filter(name => !current.has(name)));
      const refsBefore = closingRefs(before.body), refsAfter = closingRefs(after.body);
      await unmarkIssues(minus(refsBefore, refsAfter));
      await markIssues(minus(refsAfter, refsBefore));
      break;
    }

    case 'converted_to_draft':
      await labels.add(pr.number, [definitions.wip.name]);
      break;

    case 'ready_for_review':
      await labels.remove(pr.number, [definitions.wip.name]);
      break;

    case 'closed':
      await labels.remove(pr.number, [definitions.wip.name].filter(name => current.has(name)));
      // Merged issues keep In Progress until they are closed, since develop is not the default branch
      if (!pr.merged) await unmarkIssues(closingRefs(after.body));
      break;
  }
};
