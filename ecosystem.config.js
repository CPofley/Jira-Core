module.exports = {
  apps: [{
    name: "jira-backend",
    script: "java",
    args: ["-jar", "target/Jira-0.0.1-SNAPSHOT.war"], // Adjust the war name if needed
    env: {
      GITHUB_TOKEN: process.env.GITHUB_TOKEN
    }
  }]
};
