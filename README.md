# ma-pair-tool

![Build](https://github.com/Sieff/ma-pair-tool/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/25434-cps-agent.svg)](https://plugins.jetbrains.com/plugin/25434-cps-agent)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/25434-cps-agent.svg)](https://plugins.jetbrains.com/plugin/25434-cps-agent)

## Template ToDo list
- [x] Create a new [IntelliJ Platform Plugin Template][template] project.
- [x] Get familiar with the [template documentation][template].
- [x] Adjust the [pluginGroup](./gradle.properties) and [pluginName](./gradle.properties), as well as the [id](./src/main/resources/META-INF/plugin.xml) and [sources package](./src/main/kotlin).
- [x] Adjust the plugin description in `README` (see [Tips][docs:plugin-description])
- [x] Review the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html?from=IJPluginTemplate).
- [x] [Publish a plugin manually](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate) for the first time.
- [x] Set the `MARKETPLACE_ID` in the above README badges. You can obtain it once the plugin is published to the marketplace.
- [ ] Set the [Plugin Signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html?from=IJPluginTemplate) related [secrets](https://github.com/JetBrains/intellij-platform-plugin-template#environment-variables).
- [ ] Set the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html?from=IJPluginTemplate).
- [ ] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified about releases containing new features and fixes.

<!-- Plugin description -->
A plugin for a conversational agent within the IDE.

Its purpose is to be used in a study for programming tasks with an LLM-based agent.
The plugin uses the OpenAI API as a backend for the agent to generate messages.

<h3>Usage</h3>
<ol type="1">
    <li>Install the plugin</li>
    <li>Go to your IDE settings. Tools > CPS Agent</li>
    <li>Enter your OpenAI API key in order for the agent to make requests</li>
    <li>Choose a study group, value 1 or 2 will result in a different kind of agent being used</li>
    <li>Open the tool window "Assistant" on the right hand toolbar</li>
    <li>Start using your new assistant</li>
</ol>
<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "CPS Agent"</kbd> >
  <kbd>Install</kbd>
  
- Manually:

  Download the [latest release](https://github.com/Sieff/ma-pair-tool/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
