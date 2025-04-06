# Contributing to the project

First off, thank you for considering contributing to this project.
It's people like you that make it a reality for the community.

Below are a set of guidelines for contributing to this project.
These are mostly guidelines, not rules. Use your best judgment, and feel free to propose changes to
this document in a pull request.

This is an open source project, and we love to receive contributions from our community — you!
There are many ways to contribute, from writing tutorials or blog posts, improving the documentation,
submitting bug reports and feature requests or writing code which can be incorporated into the project itself.

## Types of Contributions

You can contribute in many ways:

### Report Bugs

Report bugs at the [issue tracker][issues].

If you are reporting a bug, please include:

- Your operating system name and version.
- Any details about your local setup that might be helpful in troubleshooting.
- If you can, provide detailed steps to reproduce the bug.
- If you don't have steps to reproduce the bug, just note your observations in as much detail as you can.
  Questions to start a discussion about the issue are welcome.

### Fix Bugs

Look through the GitHub issues for bugs.
Anything tagged with "bug" is open to whoever wants to implement it.

### Implement Features

Look through the GitHub issues for features.
Anything tagged with "enhancement" and "please-help" is open to whoever wants to implement it.

Please do not combine multiple feature enhancements into a single pull request.

### Submit Feedback

The best way to send feedback is to file an issue at [issue tracker][issues].

If you are proposing a feature:

- Explain in detail how it would work.
- Keep the scope as narrow as possible, to make it easier to implement.
- Remember that this is a volunteer-driven project, and that contributions are welcome :)

## Ground Rules

### Do

* Ensure cross-platform compatibility for every change that's accepted. Windows, Mac, Debian & Ubuntu Linux.
* Use conventional commits the right way. If you are not familiar with it, please check [this](https://www.conventionalcommits.org/en/v1.0.0/) out.
* Create issues for any major changes and enhancements that you wish to make. Discuss things transparently and get community feedback.
* Be welcoming to newcomers and encourage diverse new contributors from all backgrounds.

### Don't

* Misuse the commit messages. They are responsible for determining the correct version and are taken very seriously.
* Add any form of copyrighted material to the repository.
* Use any form of offensive language or images in the repository.
* Make changes that go against the core values of the project.

## Pull Request Guidelines

* Always branch off from `main` when creating a new branch or forking.
* A Pull Request should address one and only one concern. When you have multiple changes to propose, please create multiple PRs.
* If the Pull Request adds functionality, the README.md and all documentation should be updated accordingly.
* Do NOT submit WIP (Work in Progress) pull requests. We only accept PRs that are ready for review.
* Pull Requests must have passing CI/CD checks before they can be merged.
* If the CI/CD checks fail, please address the issues and push additional commits to the same branch. The PR will not be reviewed until the checks pass.

## Development

This project uses Kotlin with Gradle for development.

### Setting up

1. Clone the repository.
2. Open the project in your favorite IDE.
3. Run `./gradlew build` to build the project.

If the build is successful, you are ready to start developing.
If you encounter any issues, please follow the steps in the error message first and then ask for help in the [Discord server][discord].

The project uses [Stonecutter][stonecutter] via [Stonecraft][stonecraft] for managing different versions of Minecraft within the same codebase.

[issues]: https://github.com/KyrptonaughtMC/Inventory-Sorter/issues
[stoecraft]: https://stonecraft.meza.gg/
[stonecutter]: https://stonecutter.kikugie.dev/
[discord]: https://discord.gg/dvg3tcQCPW
[architectury]: https://docs.architectury.dev/
