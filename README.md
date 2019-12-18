# uCube mPOS SDK Android

## General Architecture

## Transaction Flow : SMC

## Transaction Flow : NFC

## UCubeAPI : Initialization
------
* This API initializes the sdk by initializing differents modules; RPC, Payment, MDM…
* It takes in input the YTMPOSProduct that user of SDK choose to use.
* It can throws two type of exception : BleNotSupportException and BluetoothNotSupportException.
* BleNotSupportException : mean that the YTMPOSProduct specified was the uCube_Touch and the used smartphone don’t supports BLE.
* BluetoothNotSupportException : mean that the used smartphone don’t support Bluetooth. 
* User should call this API before start using any other API of SDK. 

	

		'try {
			UCubeAPI.init(getApplicationContext(), activity, YTMPOSProduct.uCube_touch);

		} catch (BleNotSupportException e) {
	  		e.printStackTrace();
		} catch (BluetoothNotSupportException e) {
 	  		e.printStackTrace();
		}'


		&nbsp;
------
[www.youtransactor.com](https://www.youtransactor.com)

## UCubeAPI : Payment

## UCubeAPI : Update + send Logs

## RPC : Call command