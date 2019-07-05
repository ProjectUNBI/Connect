# Connect
Tasker plugin for triggering or transfering message between devices... AES encrypted message and one time trigger message


# Features
* Tasker app integrations
* Three standalone functions : 
   * 1) Find phone 
   * 2) Copy text from another devices 
   * 3) Send clipboard content to another device
* Work with EventGhost.Eventghost Plugin-> [see](https://github.com/ProjectUNBI/Connect/tree/master/EventGhostPlugin/Connect) 
* AES-128 CBC encryption
* An action is triggered by a unique one time message only
* Uses TCP socket type communication
* Dont use SSL/TLS


# Why not Using SSL/TLS?
* Actually i am neither IT Profesional nor Professional Programmer/Developer(it is my hobby). So I don't fully understand what is SSL/TLS thoroughly
  so i don't want to implement it
* As we directly set up all the devices, we dont need the assymetric encryption type which use by SSL/TLS. We can use symmetric type of encryption as the password can be manually setup.
* I think SSL/TLS will make Arduino and ESP module heavier (if we add this feature later on)

# How it work?
* All the message transmission is AES-128 CBC encryption, so lets assume the message is unhackable.
* What we worry now is triggering the task by the same encypted message where the bad guy sent to the device again ang again (if he listened our message)
* So for every communication there is two step.
* The first one, we will transferred unique uuid string (salt) between the two device. No one can decrypt this(as we assumed earlier😊).
  These salt will store in its respective devices for some time(around 10minute. after that the salts will automatically deleted)
* Then after the first communication we will send the real message where we added the salt given by the other device in the first communication.
  As we added the unique salt to the message, the encrypted mesage will be unique. and the message will be valid till the salt is valid 
* The reciever will decrypt the message and will check if the salt is valid or not. and the the task will be performed if the salt is valid

# How to set up
There are two type of connectio:
* Android and Android(via Tasker app)
* Android and Windows(via Tasker and Eventghost)

# First things First
* it will not work in dynamic type of ip
* You need to set up the devices into static ips.
  It can be done from your router or from your devicec. For setting up static ip from router,go to the router setting and assign each device's MAC address with specific static ip.If you want to assign from your devices, for example from your android phone, Go to the setting>wifi>connect to the wifi>long press the wifi and click on "Modify network">go to advance options>Changes Ip setting from DHCP to "Static" and fill up it.

# Android to Android (via Tasker)
* Download the app
* Set up static ip for each device
* Connect the devices in same Wifi/LAN
* Open the "Connect" app
* fill the IP field with respective static IP of the devices
* Dont forget to set the same encryption passord for all devices

Ok..You set up the "Connect" app
Now lets configure the Tasker app:
lets assume two device "Device X" as message sender and "Device Y" as message receiver

In Device X:
* go to Tasker>Create new Task>add Task>Plugin>"Connect"
* while editting=> select the "This is a message". Write the receiver IP addres and port... fill up the TAG and MESSAGE with some text. (Note you should remember this as this will be required in receiver).Now, save the setting


In Devicec Y:
* Add Event>Plugin>"Connect">Configuration
* Sellect the "Message" option and fill up the Tag and message
* save it after selecting some task for that profile


Now from the device X, Perform the task... You will see the another task triggered in the other phone. Please note that you have to connect the decvices in the same Local area network (LAN) or WIFI and You also have to assign Static Ip for each device and also the Password must be same.


# For Android and Windows's event ghost:

* https://github.com/ProjectUNBI/Connect/tree/master/EventGhostPlugin/Connect download the file and save in a folder named "Connect"
Move this folder to the eventghost Plugin's location. e.i. "C:\Program Files (x86)\EventGhost\plugins". Now restart the EventGhost.
Now You will be able to see the Connect plugin in eventghost,in "Autostart>StartPlugin" as "Connect ghostly". Set up the port and Password (same with other devices).

Now You can see events occur in Eventghost's Log. when you send a message with Tag from tasker app to this device's ip address.

For sending message from Eventghost, run the following code as python script in Eventghost

```
#DONT CHANGE THIS CODE BLOCK
TYPE_RESPOSNE = 1#DONT CHANGE THIS CODE BLOCK
TYPE_MESSAGE = 2#DONT CHANGE THIS CODE BLOCK

RESULT_UNKNOWN = 0#DONT CHANGE THIS CODE BLOCK
RESULT_SUCCESS = 1#DONT CHANGE THIS CODE BLOCK
RESULT_FAILURE = -1#DONT CHANGE THIS CODE BLOCK
def sendCommand(receiverIP,receiverPort,password,messageType,isIntent,Tag,Taskname,Message=None,extra=None, resultCode=RESULT_UNKNOWN):#DONT CHANGE THIS CODE BLOCK
    eg.plugins.MyPlugin.SendMessage(receiverIP,receiverPort,password,messageType,isIntent,Tag,Message,Taskname,extra,resultCode)#DONT CHANGE THIS CODE BLOCK
####################



receiverIP='192.168.2.254'
receiverPort='6868'
password='your password'

messageType=TYPE_MESSAGE
isIntent=False  #it is only for android
Tag='yourtag'
Taskname='taskname'

sendCommand(receiverIP,receiverPort,password,messageType,isIntent,Tag,Taskname)

```


# For Using the Custom task:
* Our app support three custome task 1)Copy text to the clipboard(when a message receive containg text)  2) Sending clipboard content back to another devicec 3)Find de device

* Just turn on the "Enable custom activity" in the App's main page
* Go to App's "Costom Activity">"Find phone" and select it..you will see "waiting message"
* At this time send the message to trigger this task from another device
* After you receive the message, press ok...
* Now you can trigger this task from the other devices
