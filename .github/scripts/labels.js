const definitions = require('../labels.json');

module.exports = (github, context) => ({
  definitions,

  async add(issue_number, names) {
    if (names.length) {
      await github.rest.issues.addLabels({ ...context.repo, issue_number, labels: names });
      console.log(`#${issue_number} + ${names.join(', ')}`);
    }
  },

  async remove(issue_number, names) {
    for (const name of names) {
      try {
        await github.rest.issues.removeLabel({ ...context.repo, issue_number, name });
        console.log(`#${issue_number} - ${name}`);
      } catch (error) {
        if (error.status !== 404) throw error;
      }
    }
  },
});
