# NS8 connector for SAP Commerce

## Overview

This is the source code for the NS8 connector for SAP Commerce. Below you’ll find the instructions and source code to install NS8 in your SAP store. If you need any help, please reach out to support@ns8.com and we’ll be happy to help you.

## Prerequisites

In order to install NS8 you must have:

- SAP Commerce Platform 1905 or higher
- B2C Accelerator (included with your SAP Commerce package)

## Install NS8 connector

1. Unzip the provided .zip file.
2. Copy the extracted folders to the `${HYBRIS_BIN_DIR}` folder of your SAP installation.
3. Run the `ant clean` command from within your SAP Commerce `bin/platform` directory.
4. Copy the following lines into your `localextensions.xml` after `<path dir="${HYBRIS_BIN_DIR}"/>` :

    <path autoload="true" dir="${HYBRIS_BIN_DIR}/modules/ns8-protect"/>

    > 📝 **Note:** ****The extensions do not rely on absolute paths, so you can place the extensions in a different location (such as `${HYBRIS_BIN_DIR}/custom`) as long as you provide the correct path in Step 4.

5. Run the following commands to install the AddOns on the yaccelatorstorefront (replace "yacceleratorstorefront" with the name of your custom storefront):

    ant addoninstall -Daddonnames="ns8addon" -DaddonStorefront.yacceleratorstorefront="yacceleratorstorefront"

## Create sample cronjobs

The `ns8sampledataaddon` is optional, and can be installed by following these steps:

1. Install the AddOn using the following code:

    ant addoninstall -Daddonnames="ns8sampledataaddon" -DaddonStorefront.yacceleratorstorefront="yacceleratorstorefront"

2. Run the `ant clean all` command from within your SAP `bin/platform` directory.
3. Run the `hybrisserver.sh` to start up the SAP server.
4. Update your running system using the `ant updatesystem` command.

## Install NS8 using the provided Gradle recipes

NS8 provides one built-in Gradle recipe to be used with the SAP Commerce installer.

To leverage this feature, locate the recipe `b2c_acc_plus_ns8_protect` with B2C Accelerator and NS8 functionality.

To install the AddOn using this recipe, run the following commands:

    # Create a solution from the accelerator templates, and install the addons:
    HYBRIS_HOME/installer$ ./install.sh -r b2c_acc_plus_ns8_protect setup

    # Build and initialize the platform:
    HYBRIS_HOME/installer$ ./install.sh -r b2c_acc_plus_ns8_protect initialize

    # Start your commerce suite instance:
    HYBRIS_HOME/installer$ ./install.sh -r b2c_acc_plus_ns8_protect start

> 🗒 **Note:** This recipe is based on the `b2c_acc_plus` recipe provided by SAP and can be found in the `installer` folder.
> 💡**Tip****:** To use this recipe on a clean SAP Commerce installation, copy the `connector` folder to your `${HYBRIS_BIN_DIR}/modules`.
