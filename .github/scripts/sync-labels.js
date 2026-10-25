module.exports = async ({ github, context, core }, prune) => {
  const definitions = Object.values(require('../labels.json'));
  const existing = await github.paginate(github.rest.issues.listLabelsForRepo, { ...context.repo, per_page: 100 });
  const byName = new Map(existing.map(label => [label.name, label]));

  for (const { name, color, description } of definitions) {
    const label = byName.get(name);
    if (!label) {
      await github.rest.issues.createLabel({ ...context.repo, name, color, description });
      core.info(`Created ${name}`);
    } else if (label.color.toLowerCase() !== color.toLowerCase() || (label.description || '') !== description) {
      await github.rest.issues.updateLabel({ ...context.repo, name, color, description });
      core.info(`Updated ${name}`);
    }
  }

  const defined = new Set(definitions.map(definition => definition.name));
  for (const { name } of existing.filter(label => !defined.has(label.name))) {
    if (prune) {
      await github.rest.issues.deleteLabel({ ...context.repo, name });
      core.info(`Deleted ${name}`);
    } else {
      core.info(`Not defined in labels.json: ${name}`);
    }
  }
};
