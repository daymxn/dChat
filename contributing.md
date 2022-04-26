# Contributing

Any and all contributions are entirely welcomed! Before you contribute though, there are
some things you should know.

## Getting started

*[Almost]* Every subdirectory has a markdown file with explanations on how code is
structured and designed in that layer. It will give you a brief overview of what the layer
is, and then walk you through on how to contribute to it. You can use the table below to
quickly navigate to any of them.

- [Datamodels](/src/main/kotlin/com/daymxn/dchat/datamodel/DATAMODELS.md)
- [Routes](/src/main/kotlin/com/daymxn/dchat/routes/ROUTES.md)
- [WebSockets](/src/main/kotlin/com/daymxn/dchat/routes/websockets/WEBSOCKETS.md)
- [Services](/src/main/kotlin/com/daymxn/dchat/service/SERVICES.md)
- [Tests](/src/test/TESTS.md)

## Making changes

To make changes, clone the repo to your local disk

`git clone git@github.com:daymxn/dChat.git`

Then, checkout to a new feature branch labeled in the following format

`git checkout -b NAME-CATEGORY-FEATURE`

Where `NAME` is your *firstLast* name or your *github* username. `CATEGORY` is something like; feature or bugfix.
And `FEATURE` is the title of the new feature (or bug) you're contributing for.

After you've made changes to your local branch, and you want to submit, you can open a Pull Request (PR)
via the [GitHub web panel](https://github.com/daymxn/dChat/compare).

> Note that making public contributions to this repo means you accept the LICENSE in place, and are contributing code that also respects that same license

### Code Formatting

Code in this repo is formatted with the ktfmt tool for google-java-format. You can enable
this formatting in IntelliJ by downloading and installing the
[ktfmt plugin](https://github.com/facebookincubator/ktfmt).
