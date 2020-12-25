# Initial Setup

This lazy is a modified version of https://www.home-assistant.io/integrations/google_assistant/

To use Google Assistant, your server has to be **externally accessible with a hostname and SSL certificate**. If you haven't already configured that, you should do so before continuing. If you make DNS changes to accomplish this, please ensure you have allowed up to the full 48 hours for DNS changes to propagate, otherwise Google may not be able to reach your server.

You will need to create a service account Create Service account key which allows you to update devices without unlinking and relinking an account (see below). If you don't provide the service account, the google_assistant.request_sync service is not exposed. It is recommended to set up this configuration key as it also allows the usage of the following command, “Ok Google, sync my devices”. Once you have set up this component, you will need to call this service (or command) each time you add a new device.

## Configure Google Assistant
* Go to https://console.actions.google.com/
* Create a new project in the Actions on Google console.
* Click New Project and give your project a name.
* Click on the Smart Home card, then click the Start Building button.
* Click Build your Action, then click Add Action(s).
* Add your Home Assistant URL: https://[HAL URL:PORT]/api/google_assistant in the Fulfillment URL box.
* Click Save.
* Click on the Overview tab, which will lead you back to the app details screen.

Account linking is required for your app to interact with the server.
* Clicking on Setup account linking under the Quick Setup section of the Overview page.
    * If asked, leave options as they default No, I only want to allow account creation on my website and select Next.
    * Then if asked, for the Linking type select OAuth and Authorization Code. Click "Next".
* Enter the following: 
    * Client ID: https://oauth-redirect.googleusercontent.com/r/YOUR_PROJECT_ID. (Find your "YOUR_PROJECT_ID" by clicking on the three little dots (more) icon in the upper right corner of the console, selecting Project settings, your Project ID will be listed on the GENERAL tab of the Settings page.) 
    * Client Secret: Anything you like, Hal doesn't need this field.
    * Authorization URL (replace with your actual URL): https://[HAL URL:PORT]/api/assistant/google/auth/authorize.
    * Token URL (replace with your actual URL): https://[HAL URL:PORT]/api/assistant/google/auth/token.
    * Click Next, then Next again.
* In the "Configure" your client Scopes textbox:
    * type "email" and click Add scope,
    * then type "name" and click Add scope again.
    * Do NOT check Google to transmit clientID and secret via HTTP basic auth header.
    * Click Next, then click Save


### On Phone
* Open the Google Home app and go to Settings.
* Click Add... --> Set up or add --> Set up device --> and click Have something already setup?. You should have [test] your app name listed under ‘Add new'. Selecting that should lead you to a browser to login your Home Assistant instance, then redirect back to a screen where you can set rooms and nicknames for your devices if you wish.

## Active Reporting
If you want to support actively reporting of state to Google's server (configuration option report_state) and support google_assistant.request_sync, you need to generate a service account.
* In the GCP Console, go to the "Create Service account" key page.
* From the Service account list, select "New service account".
* In the "Service account name" field, enter a name.
* In the "Service account ID" field, enter an ID.
* From the Role list, select "Service Accounts > Service Account Token Creator".
* For the Key type, select the JSON option.
* Click "Create". A JSON file that contains your key downloads to your computer.
* Use the information in this file, or the file directly to add to the service_account key in the configuration.
* Go to the "Google API Console".
* Select your project and click "Enable HomeGraph API".

## Multiuser
If you want to allow other household users to control the devices:
* Open the project you created in the Actions on Google console.
* Click Test on the top of the page, then click Simulator located to the page left, then click the three little dots (more) icon in the upper right corner of the console.
* Click Manage user access. This redirects you to the Google Cloud Platform IAM permissions page.
* Click ADD at the top of the page. 
    * Enter the email address of the user you want to add. 
    * Click Select a role and choose Project < Viewer.
    * Click SAVE 
    * Copy and share the link with the new user.
    * When the new user opens the link with their own Google account, it will enable your draft test app under their account.
* Have the new user go to their Google Assistant app to add [test] your app name to their account.

# Plugin Configuration

|Config Parameter           |Value           |Description |
|---------------------------|----------------|------------|
|assistant.google.port      |Port number     |The port where radio dongle is connected|
|assistant.google.client_id |String client ID|A value matching client ID configured in Google Actions Console|
