const TITLE_TYPES = [
  ['bug', /\b(bug|crash|error|exception)\b/i],
  ['featureRequest', /\b(feature|suggestion|request)\b/i],
];
const URGENT_TITLE = /\b(urgent|critical)\b/i;
/** Issue form rendering of the feature_request.yml priority dropdown */
const CRITICAL_PRIORITY = /^### Priority\s+Critical\b/m;

module.exports = async ({ github, context }) => {
  const labels = require('./labels.js')(github, context);
  const { definitions } = labels;
  const { action, issue } = context.payload;
  const current = new Set(issue.labels.map(label => label.name));
  const has = key => current.has(definitions[key].name);

  switch (action) {
    case 'opened': {
      const add = [];
      if (!has('bug') && !has('featureRequest')) {
        const type = TITLE_TYPES.find(([, pattern]) => pattern.test(issue.title));
        if (type) add.push(definitions[type[0]].name);
      }
      if (!has('highPriority') && (URGENT_TITLE.test(issue.title) || CRITICAL_PRIORITY.test(issue.body || ''))) {
        add.push(definitions.highPriority.name);
      }
      await labels.add(issue.number, add);
      break;
    }

    case 'closed':
      await labels.remove(issue.number, ['inProgress', 'stale'].filter(has).map(key => definitions[key].name));
      if (issue.state_reason === 'completed' && !has('done')) {
        await labels.add(issue.number, [definitions.done.name]);
      }
      break;

    case 'reopened':
      if (has('done')) await labels.remove(issue.number, [definitions.done.name]);
      break;
  }
};
