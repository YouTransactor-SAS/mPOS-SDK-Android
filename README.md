# uCube mPOS SDK Android
![Cptr_PlatformAPI](https://user-images.githubusercontent.com/59020462/71244593-2b897180-2313-11ea-95af-8a2fcce628eb.jpeg)

This repository provides a step by step documentation that will allow you to integrate our uCube library for Android SDK to develop your proper application. To do it just follows the instruction.

For more information about YouTransactor developer products, please refer to our [www.youtransactor.com](https://www.youtransactor.com).


## I. Context
### 1 General overview 
YouTransactor mPOS products are : 
* uCube (with different models)
* uCube Touch

The uCube Touch is a new version of the uCube. There are some hardware differences, which are: 
* The uCube use the classical Bluetooth and the uCube Touch use the BLE 
* The uCube provide a magstripe reader but not the uCube Touch

The uCubeLib support these two product. There is a setup () API implemented by the uCubeLib which takes a YTMPOSProduct. It initializes the SDK to connect one of this two products. In this document “uCube” is used as a name for this two products.

#### 1.1 uCube
The uCube is a lightweight and compact payment dongle. It can turn a tablet or a mobile device, Android or iOS, into a point of sale, via a Bluetooth connection to enable acceptance of magstripe, contactless and smart payment cards (depending on the model).

//TODO  add image

#### 1.2 uCube Touch
The uCube Touch is a lightweight and compact payment dongle. It can turn a tablet or a mobile device, Android or iOS, into a point of sale, via a BLE connection to enable acceptance of contactless and smart payment cards.

//TODO add image

#### 1.3 Mobile Device
The mobile device can be either Android or iOS and typically hosts applications related to payment. It links the uCube / uCube Touch to the rest of the system.

The mobile device application consists of 2 modules:
* Business module
	* Application that meets the business needs of the end customer. This is for example a cashier    	    application in the case of a restaurant, or a control application in the case of transports.
* "uCubeLib" Module
	* Manages the Bluetooth connection with the uCube / uCube Touch
	* Drives the transaction between the uCube and the payment card.
	* Responsible for uCube software updates

The business module on the mobile device is developed by the integrator. It uses the user interfaces of the mobile device to fulfill the business needs of the customer. A sample application is provided to the integrator, under the SDK.

The uCubeLib module is developed by YouTransactor. It is delivered to the integrator as a library, and compiled with the business module to generate the payment application.

The purpose of this document is to describe the services provided by the uCubeLib module to the business module.

#### 1.4 The Management System
The management system is administered by YouTransactor and offers the following services:
* Management of the uCube fleet
* Deployment of software updates
* Deployment of payment parameters
* Other services

The management system does not require integration with the business module, so its operation is not developed in this documentation.

### 2 uCube management
#### 2.1 Setup 
##### 2.1.1 Initial configuration 
To be functional, in the scope of PCI PTS requirement, and SRED key shall be loaded securely in the device. This key is loaded locally by YouTransactor tools. The initial SALT is injected in the same way.

##### 2.1.2 Bleutooth pairing
Before using the payment function, the uCube must be paired with the mobile device via Bluetooth.

This pairing can be done by a "connect" method of the uCubeLib module. It will scan all available devices and will display a system pop-up prompting the user to select the device to use.

The uCubeLib needs the Bluetooth to be enabled, it will request to enable it, if it is disabled. And if the YTMPOSProduct chosen, when setup was called, was the uCube Touch, a Location permission will be requested.

//TODO ADD IMG

#### 2.2 Switching uCube On/Off
The uCube lights up by pressing the "ON / OFF" button for three seconds. Once the device is on, the business module can detect it, and initiate the payment process. The uCube switches off either by pressing the "ON / OFF" button or after X* minutes of inactivity (* X = OFF timeout).

The uCube Touch can be lights up exactly like the uCube, but also by using “connect” method. When connection established, the SDK check if the device is state, if it is power off, it turns it 

#### 2.3 Firmware update
During the life of the uCube, the uCube firmware could be updated (for bug fix, etc..). The “checkUpdate” method make a compare between current versions of firmware and configuration with These defined in TMS server. If the is a difference or a force update parameter is set to true, it returns the update list. The method “update” used to apply the updates, it takes as input the list of updates to be applied, it can be the output of “checkUpdate” method or a sub list of that. It downloads the binaries of each update in input list and install them all. 

#### 2.4 Send logs
uCubeLib provide a “LogManager” Class used to print logs in logcat at runtime, save these logs in files. And it needed, send these files to TMS server. Logs can be enabled or disabled. The “sendLogs” method is used to send a zip file that contain all saved logs files. 

## II. Technical Overview

### 1. General Architecture

This section describes the general uCube MPOS Android SDK architecture. The SDK provide: 

* Connexion module used to scan Bluetooth devices and pair then connect uCube (classical Bluetooth and BLE supported) 
* MDM module that implements all TMS Web services to manage uCube updates and debug.
* RPC module that implements all SVPP commands. This RPC APIs used to drive the SVPP for processing transactions or to update it.
* Payment module implements magstripe, contact and contactless transaction services. It uses the RPC module to call RPC commands during the transaction.
* UCubeAPI is the public interface exposed to the integrator it implements several APIs to mainly setup the connection with uCube, do a payment, check and start a uCube update, send logs to TMS server.

The Integrator is able to use the UCubeAPI interface and call RPC commands. 

![Cptr_Architecture](https://user-images.githubusercontent.com/59020462/71239040-d8f58880-2305-11ea-97d3-9441e2b7e0d3.jpeg)

### 2. Transaction Flow : Contact

![Cptr_TransactionSMC](https://user-images.githubusercontent.com/59020462/71239375-b44de080-2306-11ea-9c32-f275a5407801.jpeg)


### 3. Transaction Flow : Contactless

![Cptr_TransactionNFC](https://user-images.githubusercontent.com/59020462/71239723-8ddc7500-2307-11ea-9f07-2f4b11b42620.jpeg)

### 4. Prerequisites

To embed the package that you need in your application, you have to be sure of certain things in your settings.
1. Received YouTransactor card terminal : uCube, uCubeTouch
2. The `minSDKVersion` must be at 21 to works properly.
3. The `targetSDKversion` 28 or later (as a consequence of the migration to AndroidX).
4. Following Google's best practices SDK 3.3.0 will migrate to AndroidX. For more information about AndroidX and how to migrate see Google AndroidX Documentation.

### 5. Dependency

Our SDK is in the format “aar” in the library. So if you want to access to it here is what you must do.
You will need to get into your app-level Build.Gradle to add this dependency:

		implementation files('libs/ucube_lib.aar')

### 6. UCubeAPI
The APIs provided by UCubeAPI modules are:

* initManagers (@Nonnull Context context)
* setup (@Nonnull Context context, @NonNull Activity activity, @NonNull YTMPOSProduct ytmposProduct, @Nonnull UCubeAPIListener uCubeAPIListener)
* YTMPOSProduct getYTMPOSProduct()
* connect (@Nonnull Activity activity, @NonNull UCubeInitListener uCubeInitListener)
* UCubeInfo getUCubeInfo()
* deletePairedUCube()
* pay(@Nonnull Context context, @Nonnull UCubePaymentRequest uCubePaymentRequest, @Nonnull UCubePaymentListener uCubePaymentListener)
* checkUpdate(@NonNull Activity activity, boolean updateSameVersion, boolean doNotCheckVersion, boolean checkOnlyFirmwareVersion, @Nonnull UCubeCheckUpdateListener uCubeCheckUpdateListener)
* update(@NonNull Activity activity, final @NonNull List<BinaryUpdate> updateList, @Nonnull UCubeAPIListener uCubeAPIListener)
* sendLogs(Activity activity, @Nonnull UCubeAPIListener uCubeAPIListener)
* close()


## II. Integrate the uCube mPOS SDK Android
* You can use the sample app provided in this repository as a reference

### 2. UCubeAPI : initManagers (...)
* This API initializes the SDK. It should be called in the begining, before calling any other API. 


* This API initializes the SDK by initializing differents modules; RPC, Payment, MDM…
* It takes in input the YTMPOSProduct that user of SDK chooses to use.
* It can throw two types of exception: BleNotSupportException and BluetoothNotSupportException.
* BleNotSupportException : mean that the YTMPOSProduct specified was the uCube_Touch and the used smartphone don’t support BLE.
* BluetoothNotSupportException : mean that the used smartphone doesn’t support Bluetooth. 
* User should call this API before start using any other API of SDK. 
	
		
		try {
			UCubeAPI.init(getApplicationContext(), activity, YTMPOSProduct.uCube_touch);
		} catch (BleNotSupportException e) {
	  		e.printStackTrace();
		} catch (BluetoothNotSupportException e) {
 	  		e.printStackTrace();
		}
		




### 3. UCubeAPI : Payment


![Cptr_Payment](https://user-images.githubusercontent.com/59020462/71241849-e3675080-230c-11ea-91ac-996a36382556.jpeg)

#### Payment request
* This API start payment by activating available readers in the device. (NFC, SMC, MSR)
* It takes in input a UCubePayRequest and gives in output a UCubePayResponse.
* The payment params that the user should specify are :
	- [ ] **Amount**
	- [ ] **Currency**  // CURRENCY_EUR or CURRENCY_USD or new Currency(iso_code, exponent, label) 
	- [ ] **Transaction type** // PURCHASE /  WITHDRAWAL  / REFUND /  PURCHASE_CASHBACK / MANUAL_CASH / INQUIRY
	- [ ] **Card wait timeout**
	- [ ] **Application selection Task** // Instance of class which implements IApplicationSelectionTask, if null SDK will use the default ApplicationSelectionTask.
	- [ ] **Authorization task** // Instance of class that implements  IAuthorizationTask.
	- [ ] **RiskManagement task** // Instance of class that implements  IRiskManagementTask.


				UCubePaymentRequest paymentRequest = new UCubePaymentRequest.Builder()
					.setAmount(1.0)
					.setCurrency(UCubePaymentRequest.CURRENCY_EUR)
					.setAuthorizationTask(new MyAuthorizationTask())
					.setRiskManagementTask(new MyRiskManagementTask())
					.build();
				UCubeAPI.pay(activity, paymentRequest,  PAYMENT_REQUEST_CODE)



#### Example of Application selection task

		public class MyApplicationSelectionTask implements IApplicationSelectionTask {

  			private List<EMVApplicationDescriptor> applicationList;
  			private List<EMVApplicationDescriptor> candidateList;
  			private PaymentContext context;

			@Override
			public void setAvailableApplication(List<EMVApplicationDescriptor> applicationList) {   this.applicationList = applicationList; }

			@Override
			public List<EMVApplicationDescriptor> getSelection() { return candidateList;  }

			@Override
			public PaymentContext getContext() {  return context;  }

			@Override
			public void setContext(PaymentContext paymentContext) {  this.context = paymentContext; }
			 
			@Override
			public void execute(ITaskMonitor monitor) {
			    candidateList = new ArrayList<>();

			    // todo

			    monitor.handleEvent(TaskEvent.SUCCESS);
  			}
		}



#### Example of Risk Managament task

		public class MyRiskManagementTask implements IRiskManagementTask {

  			private ITaskMonitor monitor;
  			private PaymentContext paymentContext;
  			private byte[] tvr;

  			@Override
  			public byte[] getTVR() {  return tvr; }

  			@Override
  			public PaymentContext getContext() {  return paymentContext; }

  			@Override
  			public void setContext(PaymentContext context) {  this.paymentContext = context; }

  			@Override
  			public void execute(ITaskMonitor monitor) {
     			this.monitor = monitor;
    
        			// todo

     			monitor.handleEvent(TaskEvent.SUCCESS);
  			}
		}



#### Example of Authorization task

		public class MyAuthorizationTask implements IAuthorizationTask {

  			private byte[] authResponse;
  			private ITaskMonitor monitor;
  			private PaymentContext paymentContext;

  			@Override
  			public byte[] getAuthorizationResponse() {   return authResponse;  }

  			@Override
  			public PaymentContext getContext() {   return paymentContext; }

 			@Override
  			public void setContext(PaymentContext context) { this.paymentContext = context;  }

  			@Override
  			public void execute(ITaskMonitor monitor) {
     			this.monitor = monitor;

      			//TODO
  			}
		}



#### Additional info :

The UCubePayRequest has an optional attribute that can be used to add a list of tags.
The content of these tags will be returned in the response as UCubePayResponse.requestedTags attribute: byte[ ].

		UCubePayRequest paymentRequest = new UCubePayRequest.Builder(...)
		.setRequestTags(tags)
		.build();

		UCubeAPI.pay(activity, paymentRequest,  PAYMENT_REQUEST_CODE);



#### Payment response

Several response fields are available when the callback activity is called. 

- [ ] TxStatus : Possible Values :
	* DEFAULT_INIT
	* NFC_MPOS_ERROR
	* CARD_WAIT_FAILED
	* CANCELLED
	* STARTED
	* CARD_REMOVED
	* CHIP_REQUIRED
	* UNSUPPORTED_CARD
	* TRY_OTHER_INTERFACE
	* REFUSED_CARD
	* ERROR
	* AUTHORIZED
	* APPROVED
	* DECLINED

- [ ] TxCode : int

- [ ] TxInfo : Transaction info object containing informaation about this transaction. It contains the following information:
	* ProductInfomation : ProductInfo
	* AppVersion : String
	* TxDate : Date
	* TxAmount : double
	* TxCurrency : Currency
	* TxType : TransactionType
	* EntryMode : e.g. CHIP
	* NumberOfInstallments : int
	* CardType : e.g. MASTERCARD
	* Last for digits of the card : String
	* SelectedApplication : EMVApplicationDescriptor



#### Handle Payment result

		@Override
		protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
   			super.onActivityResult(requestCode, resultCode, data);

   			if( requestCode == PAYMENT_REQUEST_CODE)
   			// TODO parse response
		}




### 4. UCubeAPI : Update 


* The update API registers the device in YT MDM then it retrieves the current svpp version and check if it is different from configured version or not. If it is different an update process is executed.
* It takes ForceUpdate: boolean as a param. If this param true the API will update the svpp even if it has the same version as the configured one.
		
		UCubeAPI.update(forceUpdate);


### 5. UCubeAPI : Send Logs


* uCube SDK manages a log back that save all RPC exchanges and differents users actions.
* User of SDK can send logs to be interpreted by the support team using this API: 
		
		UCubeAPI.sendLogs();




### 6. RPC : Call command


* This library allows the user to call differents RPC e.g. DisplayMessageWithoutKI, GetInfo, etc.
* User may want to call some RPC, it depends on implementation of one of the tasks “Application Selection Task”, “Risk Management Task” or “Authorization Task”.
* This is an example of DisplayMessageWithoutKI command call: 

		DisplayMessageCommand displayMessageCommand = new DisplayMessageCommand(msg);

		displayMessageCommand.setCentered(centred);
		displayMessageCommand.setYPosition(yPosition);
		displayMessageCommand.setFont(font);

		displayMessageCommand.execute(new ITaskMonitor() {

  			@Override
  			public void handleEvent(TaskEvent event, Object... params) {
     			switch (event) {
     				case FAILED:
     				//TODO
        			break;

     			case SUCCESS:
    				//TODO
        			break;
     			}
  			}
		});


![Cptr_logoYT](https://user-images.githubusercontent.com/59020462/71242500-663cdb00-230e-11ea-9a07-3ee5240c6a68.jpeg)
