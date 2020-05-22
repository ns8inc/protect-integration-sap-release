# Installation and Usage

## Installing the Plugin into hybris with ns8-protect fraud avoidance functionality

First ensure that the version of hybris being used is supported for the plugin. Please view the Compatibility section for the current list of supported versions.

The plugin contains several hybris extensions. Take the following steps to include the plugin into your hybris application:

1. Unzip the supplied plugin zip file

2. Copy the extracted folders to the ${HYBRIS_BIN_DIR} of your hybris installation.

3. Run the ‘ant clean’ command from within your hybris bin/platform directory.

5. Copy the following lines into your localextensions.xml after <path dir="${HYBRIS_BIN_DIR}"/>. The extensions do not rely on any absolute paths so it is also possible to place the extensions in a different location (such as ${HYBRIS_BIN_DIR}/custom).
- &lt;path autoload="true" dir="${HYBRIS_BIN_DIR}/modules/ns8-protect"/>

6. Run the following commands to install the AddOn's on the yaccelatorstorefront (replace "yacceleratorstorefront" with your custom storefront if relevant)

- If you are installing the B2C AddOn:
- ant addoninstall -Daddonnames="ns8addon" -DaddonStorefront.yacceleratorstorefront="yacceleratorstorefront"

### Optional


1. Run the ‘ant clean all’ command from within your hybris bin/platform directory.

2. Run the hybrisserver.sh to startup the hybris server.

3. Update your running system.using "ant updatesystem"

Except for setting up your hosts file, the ns8-protect AddOn will work initially without any external setup needed.


## Installing the Plugin using the provided recipes

The AddOn provides one gradle recipes to be used with the hybris installer.

1. b2c_acc_plus_ns8_protect with b2c and ns8-protect functionality.

The recipes are based on the b2b_acc_plus recipes provided by hybris.

The recipes can be found under the installer folder.

To use the recipes on a clean hybris installation, copy the folder hybris to your ${HYBRIS_BIN_DIR}

Since the recipe generates the local.properties file with the properties defined in the recipe, optionally you can add your local.properties to the customconfig folder.

In order to install the AddOn using one of the recipes, run the following commands:
- This will create a solution from the accelerator templates, and install the addons.
HYBRIS_HOME/installer$ ./install.sh -r [RECIPE_NAME] setup
- This will build and initialize the platform
HYBRIS_HOME/installer$ ./install.sh -r [RECIPE_NAME] initialize
- This will start a commerce suite instance
HYBRIS_HOME/installer$ ./install.sh -r [RECIPE_NAME] start
