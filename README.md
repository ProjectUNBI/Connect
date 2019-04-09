# Connect
Tasker plugin for triggering or transfering between devices... AES encrypted message and one time trigger message


# Features
* Tasker app integrations
* Work with Event ghost plugin [see](https://github.com/ProjectUNBI/Connect/tree/master/EventGhostPlugin/Connect) 
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

#### Sorry for the poor documentation in the source code of the app. i will add slowly
 
