# IsoWatch App

[![Status badge](https://img.shields.io/badge/status-completed-blue.svg)](https://shields.io/)

This is the WearOS App repository for Capstone Project: IsoWatch.

Samsung Privileged SDK is used on this repository and is **not included** here. Please refer to the **[official page](https://developer.samsung.com/health/privileged/overview.html)** for more information about access to the SDK.

This project is not yet 100% complete. Our targets can be found on the **[issues](https://github.com/aditydcp/isowatch-app/issues)**.

*Note*

Some things might change or not work due to Heroku new policy effective per Nov 28, 2022.

*Last deployed: Nov 25, 2022*

## About the Project

This project is a Capstone Project for our final year assignment. This is a simulation of IoT project using a wearable thing (in this case, WearOS wearables) to collect data, transmit it to server and have the data be presented on the client web app.

Tools used in this project:
* **Client-side Web App**
  * React.js
    * Rechart: for displaying graphs
    * React Hook Form: for making forms
    * Axios: for making API calls
    * Universal Cookie: for cookie management
  * Pusher: for listening on events of database change
* **Server-side Web App**
  * Node.js & Express.js
    * Mongoose: for creating collection schema and MongoDB connection, and also watching for database change
    * Body parser: for parsing JSON request bodies
    * Jsonwebtoken: for creating token of authorization
    * bcrypt: for encrypting password
  * MongoDB: for storing data
  * Pusher: for publishing events of database change
* **WearOS App**
  * Kotlin language
    * Android Wear dependencies: for enabling Wear-specific input, layout and materials
    * Retrofit: for sending HTTP requests
    * Moshi: for parsing JSON into and from Kotlin objects
    * OkHTTP: for dealing with backward compatibilities
    * Samsung Privileged SDK: for accessing sensors

**[Server-side repository]** can be found [here](https://github.com/aditydcp/isowatch-backend).

**[Client-side repository]** can be found [here](https://github.com/aditydcp/isowatch-frontend).

As this project is not yet 100% complete. The targets for each repository can be found on their corresponding **issues** page.

For more information about the project, please **[contact me](https://github.com/aditydcp)**.

Departemen Teknik Elektro dan Teknologi Informasi

Universitas Gadjah Mada

2022

## How To

Some things you need to have installed:

- Android Studio

To start, clone this repository and open the project on Android Studio.

Once opened, Android Studio should attempt to build the app by downloading and installing all the dependencies needed.

## Changelog

v0.4.0: init development on Work Manager to implement scheduled reading

v0.3.1: implement request to add Health Point

v0.3.0: implement HTTPS request to create Pemeriksaan

v0.2.0: implement Heart Rate and SpO2 reading

v0.1.0: init project

## Learn More about the Development

Samsung Privileged SDK is used on this repository and is **not included** here. Please refer to the **[official page](https://developer.samsung.com/health/privileged/overview.html)** for more information about access to the SDK.