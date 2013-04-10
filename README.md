# RHQ Bundle Editor

is a set of eclipse plugins (based on Eclipse juno) that helps you create [RHQ
Bundle] (https://docs.jboss.org/author/display/RHQ/Ant+Bundles)

# Features
 * Code-completion in bundle `deploy.xml` descriptor ant script
 * Validation of referenced files within bundle
 * Refactoring
 * Import/Export to Bundle Distribution File (ZIP)
 * Includes Standalone Bundle Deployer
  * you can 'Dry Run' with different input properties using 'Run
  Configurations'
  * works with externally provided Standalone Deployer

# Installation
 * clone this repo
 * `mvn install`
 * Help -> Install new Software 
  * Add -> Local
  * select $RHQ_EDITOR_REPO/cz.muni.fi.rhqeditor.update/target/repository/


Classes inherited from ant:
	RhqEditor.java
	RhqEditorCompletionProcessor.java
	RhqEditorSourceViewerConfiguration.java
	TaskDescriptionProvider.java
