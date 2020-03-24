import eg

eg.RegisterPlugin(
    name="Connect Ghostly",
    author="Glu",
    version="0.1.1",
    kind="other",
    description="This will communicate with other device in TCP"
)

from threading import Event, Thread
import socket
from Crypto import Random
from Crypto.Cipher import AES
import hashlib
import base64
import threading
import wx.lib.scrolledpanel as scrolled
import time
import uuid
import json
import re

NOTIFICATION_CHANNEL_ID = "com.unbi.connect"
BOOTCOMPLETE = "android.intent.action.BOOT_COMPLETED"
MAXIMUM_TIME = 10 * 60 * 1000
MIN_POP_TIME = 10 * 1000
UUID_PREFIX = "uuid_"
SALT_PREFIX = "salt_"
TYPE_INIT = 0
TYPE_RESPOSNE = 1
TYPE_MESSAGE = 2
RESULT_UNKNOWN = 0
RESULT_SUCCESS = 1
RESULT_FAILURE = -1
LOCAL_IP = "0.0.0.0"
TO_REDIRECT = 1
TO_TRIGGER = 2
TO_NOT_TODO = -1
MSG_INVALID = 0
MSG_VALID = 1
COMMUTYPE_WIFI=1
COMMUTYPE_BLUETOOTH=2

DATALIST_SALT = "salttype"
DATALIST_MSG = "pendingMsg"


def getCurrentmilli():
    return int(round(time.time() * 1000))


'''
CYPHER

'''


class AESCipher(object):

    def __init__(self, key):
        self.bs = 16
        self.key = hashlib.sha256(key.encode()).digest()

    def encrypt(self, raw):
        raw = self._pad(raw)
        iv = Random.new().read(AES.block_size)
        cipher = AES.new(self.key, AES.MODE_CBC, iv)
        return base64.b64encode(iv + cipher.encrypt(raw))

    def decrypt(self, enc):
        enc = base64.b64decode(enc)
        iv = enc[:AES.block_size]
        cipher = AES.new(self.key, AES.MODE_CBC, iv)
        return self._unpad(cipher.decrypt(enc[AES.block_size:])).decode('utf-8')

    def _pad(self, s):
        return s + (self.bs - len(s) % self.bs) * chr(self.bs - len(s) % self.bs)

    @staticmethod
    def _unpad(s):
        return s[:-ord(s[len(s)-1:])]

# End of AES Cypher class


class DataList:

    def __init__(self):
        self.lock=threading.Lock()
        self.lasttimepoped = getCurrentmilli()
        self.arrayOftbObject = []

    def add(self, other):
        self.lock.acquire()
        try:
            exist = False
            if (type(other) is PendingMessage):
                for pdngMsg in self.arrayOftbObject:
                    if (other.mymessage.uuidToCheck.uuid == pdngMsg.mymessage.uuidToCheck.uuid):
                        exist = True

            if (type(other) is SaltObject):
                for saltobject in self.arrayOftbObject:
                    if (other.saltString == salt.saltString):
                        exist = True

            if (exist == False):
                self.arrayOftbObject.append(other)
        finally:
            self.lock.release()

    def popexpiredObject(self):
        self.lock.acquire()
        try:
            self.lasttimepoped = getCurrentmilli()
            time = self.lasttimepoped - MAXIMUM_TIME
            newlist = []
            for timObj in self.arrayOftbObject:
                if (timObj.milli < time):
                    pass
                else:
                    newlist.append(timObj)
            self.arrayOftbObject = newlist
        finally:
            self.lock.release()            

    def popexpiredObjectAsync(self):#for poping from alreadily locked thread
        self.lasttimepoped = getCurrentmilli()
        time = self.lasttimepoped - MAXIMUM_TIME
        newlist = []
        for timObj in self.arrayOftbObject:
            if (timObj.milli < time):
                pass
            else:
                newlist.append(timObj)
        self.arrayOftbObject = newlist

    def getAndPopTimeBaseObject(self, idtocheck, mtype):
        self.lock.acquire()
        try:
            if (mtype != DATALIST_SALT and mtype != DATALIST_MSG):
                return None
            newlist = []
            returnableobj = None
            if (mtype == DATALIST_MSG):
                for tobjt in self.arrayOftbObject:
                    #print(json.dumps(tobjt.mymessage.uuidToadd.uuid))
                    #print(idtocheck.uuid)
                    if (tobjt.mymessage.uuidToadd.uuid == idtocheck.uuid):
                        returnableobj = tobjt
                    else:
                        newlist.append(tobjt)

            if (mtype == DATALIST_SALT):
                for tobjt in self.arrayOftbObject:
                    if (tobjt.saltString == idtocheck):
                        returnableobj = tobjt
                    else:
                       newlist.append(tobjt)

            self.arrayOftbObject = newlist
            return returnableobj
        finally:
            self.lock.release()

    def isValid(self, timebaesObj, mtype):  # this is for salt class only not for UUID
        self.lock.acquire()
        try:
            if (int(self.lasttimepoped) + MAXIMUM_TIME < getCurrentmilli()):
                self.popexpiredObjectAsync()
            isvalid = False
            newlis = []
            if (mtype == DATALIST_SALT):
                for salt in self.arrayOftbObject:
                    if (timebaesObj.saltString == salt.saltString):
                        isvalid = True
                    else:
                        newlis.append(salt)
            self.arrayOftbObject = newlis
            return isvalid
        finally:
            self.lock.release()
            


eg.globals.SALT_DATA_LIST = DataList()
eg.globals.PENDING_MSG_DATALIST = DataList()
eg.globals.PORT = '6868'


def mesageProcess(string, pword, mport):  # string which arreive from the message
    cipher = AESCipher(pword)
    decrypted = cipher.decrypt(string)
    decrypted=re.sub(r"(^.*\}).*?$", r'\1', decrypted)
    if (decrypted is None):
        print("Decrypted result is None\nMight be errror in password")
        return None
    # decrypted='{"isIntent":false,"resultCode":0,"saltToAdd":{"saltString":"salt_838e91e3-dcc1-482e-a34e-74e9a8330f11","milli":1553697413295},"sender":{"ip":"192.168.43.201","port":8668},"mtype":0}'
    # decrypted='{"isIntent":false}'
    #print(decrypted)
    #data = json.loads(decrypted)
    try:
        data = json.loads(decrypted)
    except:
        print("Json Parse error...")
        return
    isIntent = parsemeJson(data, 'isIntent')
    saltToAddJson = parsemeJson(data, 'saltToAdd')
    if saltToAddJson is None:
        saltToAdd = None
    else:
        saltToAdd = SaltID(parsemeJson(saltToAddJson, 'saltString'))
    saltToCheckJson = parsemeJson(data, 'saltToCheck')
    if saltToCheckJson is None:
        saltToCheck = None
    else:
        saltToCheck = SaltID(parsemeJson(saltToCheckJson, 'saltString'))
    senderjson = parsemeJson(data, 'sender')
    if senderjson is None:
        sender = None
    else:
        sender = IpPort(parsemeJson(senderjson, 'ip'), parsemeJson(senderjson, 'port'))
    tag = parsemeJson(data, 'tag')
    message = parsemeJson(data, 'message')
    mtype = parsemeJson(data, 'mtype')
    uuidToaddJson = parsemeJson(data, 'uuidToadd')
    uuidToadd = MsgUUID(parsemeJson(uuidToaddJson, 'uuid'))
    uuidTocheckJson = parsemeJson(data, 'uuidToCheck')
    uuidTocheck = MsgUUID(parsemeJson(uuidTocheckJson, 'uuid'))
    taskName = parsemeJson(data, 'taskName')
    resultCode = parsemeJson(data, 'resultCode')
    extraJson = parsemeJson(data, 'extra')
    # extra=extraJson
    myMessage = MyMessage(saltToAdd, saltToCheck, sender, tag, message, extraJson, isIntent, mtype, uuidToadd,
                          uuidTocheck, taskName, resultCode)
    if myMessage is None:
        print("Meassge parsing got null value")
        return None
    ipport = IpPort(socket.gethostbyname(socket.gethostname()), mport)
    if myMessage.saltToCheck is None:
        print("Sender salt is null,sending a valid salt")
        salttoadd = myMessage.saltToAdd
        salttocheck = SaltID().generate()
        saltobject=SaltObject(getCurrentmilli(),salttocheck.saltString)
        saltobject.addToPendings(eg.globals.SALT_DATA_LIST)
        
        newmsg = MyMessage(salttocheck, salttoadd, ipport, None, None, None, False, TYPE_INIT, None
                           , myMessage.uuidToadd)
        return MessageTaskType(TO_REDIRECT, newmsg, myMessage.sender)
    isValid = eg.globals.SALT_DATA_LIST.isValid(myMessage.saltToCheck, DATALIST_SALT)
    print("Valid pending salt counts: " + str(len(eg.globals.SALT_DATA_LIST.arrayOftbObject)))
    if isValid is not True:
        return None
    if (myMessage.mtype == TYPE_INIT):
        print("Valid salt...sending actual meaasge")
        pending_mesage = eg.globals.PENDING_MSG_DATALIST.getAndPopTimeBaseObject(myMessage.uuidToCheck, DATALIST_MSG)
        print('Total pending message: ' + str(len(eg.globals.PENDING_MSG_DATALIST.arrayOftbObject)))
        if pending_mesage is not None:
            pending_mesage.mymessage.saltToCheck  =  saltToAdd
            return MessageTaskType(TO_REDIRECT, pending_mesage.mymessage, myMessage.sender)
        else:
            print("No such pending message")
            return None
    if myMessage.mtype == TYPE_RESPOSNE or myMessage.mtype == TYPE_MESSAGE:
    	#print('Actionable message recived')
        return MessageTaskType(TO_TRIGGER, myMessage, myMessage.sender)


def parsemeJson(data, key):
    try:
        return data[key]
    except:
        return None


class IpPort:
    def __init__(self, ip, port):
        self.ip = ip
        self.port = int(port)

    def generate(ipport):
        str = str(ipport).split(':')
        if (len(str) == 2):
            return IpPort(str[0], str[1])
        return None


class PendingMessage:
    def __init__(self, mymessage, milli, wheretosend):
        self.mymessage = mymessage
        self.milli = milli
        self.wheretosend = wheretosend

    def addToPendings(self, datalist):
        datalist.popexpiredObject()
        datalist.add(self)


class MsgUUID:
    def __init__(self, uuid=""):
        self.uuid = uuid

    def generate(self):
        if (self.uuid == "" or self.uuid is None):
            self.uuid = UUID_PREFIX + str(uuid.uuid4())
            return self



class SaltObject:
    def __init__(self, milli, saltString=""):
        self.saltString = saltString
        self.milli = milli
    
    def addToPendings(self, datalist):
        datalist.popexpiredObject()
        datalist.add(self)
        
        
class SaltID:
    def __init__(self,saltString=""):
        self.saltString = saltString

    def generate(self):
        if (self.saltString == "" or self.saltString is None):
            self.saltString = SALT_PREFIX + str(uuid.uuid4())
        return self


class MyMessage:
    def __init__(self,
                 saltToAdd,  # todo make salt class
                 saltToCheck,
                 sender,
                 tag,
                 message,
                 extra,  # todo make extra class
                 isIntent,
                 mtype,
                 uuidToadd,  # todo make MsgUUID class
                 uuidToCheck,
                 taskName=None,
                 resultCode=RESULT_UNKNOWN,
                 commuType=COMMUTYPE_WIFI
                 ):
        self.saltToAdd = saltToAdd
        self.saltToCheck = saltToCheck
        self.sender = sender
        self.tag = tag
        self.message = message
        self.extra = extra
        self.isIntent = isIntent
        self.mtype = mtype
        self.uuidToadd = uuidToadd
        self.uuidToCheck = uuidToCheck
        self.taskName = taskName
        self.resultCode = resultCode
        self.commuType=commuType

    def getEncryptedMsg(self, pword):
        cypher = AESCipher(pword)
        strdump=json.dumps(self, default=lambda o: o.__dict__)
        return cypher.encrypt(strdump)

    def send(self, ipport, pword):
        encrypted = self.getEncryptedMsg(pword)
        print (json.dumps(ipport.ip))
        #print (encrypted)
        try:
            if (ipport is None):
                print("trying send.. but ip port is None")
                return

            client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            client.connect((ipport.ip, ipport.port))
            client.send(encrypted.encode())
            client.close()
            if self.mtype == TYPE_INIT:
                print('Message init...')
            else:
                print('Message Sent')
        except:
            print('Sending error')
            pass


class MessageTaskType:

    def __init__(self, mtype, message, thesender):
        self.mtype = mtype
        self.message = message
        self.thesender = thesender


class ThreadedServer(object):
    def __init__(self, host, port, pword, plugin):
        self.plugin = plugin
        self.host = host
        self.pword = pword
        self.port = port
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.sock.bind((self.host, self.port))
        print('Listening :  ' + str(socket.gethostbyname(socket.gethostname())) + ': ' + str(self.port))
        self.connect = True
        self.client = None

    def disconnect(self):
        # self.sock.shutdown(socket.SHUT_RDWR)
        self.connect = False
        if (self.client is not None):
            print('cleint closed')
            self.client.close()
        self.sock.close()
        print('diconnected...')

    def stopsock(self):
        client = socket.socket(socket.AF_INET,
                               socket.SOCK_STREAM)  # we wan to close the .accept alse so we mock connection
        client.connect(("127.0.0.1", self.port))

    def listen(self):
        self.sock.listen(5)
        self.connect = True
        while (self.connect):
            self.client, address = self.sock.accept()
            self.client.settimeout(60)
            threading.Thread(target=self.listenToClient, args=(self.client, address)).start()

    def listenToClient(self, client, address):
        size = 1024
        response = ""
        if (self.connect == False):
            client.close()
            print('Plugin is closed')
            return
        while (True):
            try:
                data = client.recv(size)
                if '\r\n' in data:
                    response = response + data
                    client.send(" ")#Here we send response back
                    client.close()
                    break;
                if data:
                    # Set the response to echo back the recieved data
                    response = response + data
                else:
                    client.close()
                    break
            except:
                client.close()
                break
        client.close()
        ##here we got the message
        #print("Already closed")
        #print(response)
        msgtasktype = mesageProcess(response, self.pword, self.port)
        if msgtasktype is None:
            print('Something is wrong')
            return

        if msgtasktype.mtype == TO_NOT_TODO:
            print("Nothing to do")
            return
        if msgtasktype.mtype == TO_REDIRECT:
            #   def send(self, ipport ,pword):
            msgtasktype.message.send(msgtasktype.thesender, self.pword)
            print("redirected to " + str(msgtasktype.thesender.ip) + ":" + str(msgtasktype.thesender.port))
            return
        if msgtasktype.mtype == TO_TRIGGER:
            # MessageTaskType(TO_TRIGGER ,myMessage,myMessage.sender)
            # def __init__(self ,mtype ,message,thesender)
            # if myMessage.mtype == TYPE_RESPOSNE or myMessage.mtype == TYPE_MESSAGE:
            # Payload    def __init__(self, saltToAdd, saltToCheck, sender, tag, message, extra, isIntent,mtype,uuidToadd,uuidToCheck,taskName=None,resultCode=RESULT_UNKNOWN):
            resp = msgtasktype.message
            payload = Payload(resp.saltToAdd, resp.saltToCheck, resp.sender, resp.tag, resp.message, resp.extra,
                              resp.isIntent, resp.mtype, resp.uuidToadd, resp.uuidToCheck, resp.taskName,
                              resp.resultCode)
            if msgtasktype.message.mtype == TYPE_RESPOSNE:
                print("Response recieved")
                self.plugin.TriggerEvent("RESPOSNE_CONNECT",payload)
                return
            if msgtasktype.message.mtype == TYPE_MESSAGE:
                print("Message recieved")
                self.plugin.TriggerEvent(msgtasktype.message.tag, payload)
                return


class ScrollPanel(scrolled.ScrolledPanel):
    def __init__(self, parent):
        scrolled.ScrolledPanel.__init__(self, parent, -1)


'''
class MyMessage(
        val saltToAdd: Salt?/*This salt is to be add in nxt message if sendAsync*/,
        val saltToCheck: Salt?/*This salt is to Check from the previously stored data*/,
        val sender: IpPort,
        val tag: String?,
        val message: String?,
        val extra: Extra?,
        val isIntent: Boolean,
        val mtype: Int,
        val uuidToadd: MsgUUID?,
        val uuidToCheck: MsgUUID?,
        val taskName: String? = null,
        val resultCode: Int = RESULT_UNKNOWN
) 

'''


class Payload:
    def __init__(self, saltToAdd, saltToCheck, sender, tag, message, extra, isIntent, mtype, uuidToadd, uuidToCheck,
                 taskName=None, resultCode=RESULT_UNKNOWN):
        self.saltToAdd = saltToAdd
        self.saltToCheck = saltToCheck
        self.sender = sender
        self.tag = tag
        self.message = message
        self.extra = extra
        self.isIntent = isIntent
        self.mtype = mtype
        self.uuidToadd = uuidToadd
        self.uuidToCheck = uuidToCheck
        self.taskName = taskName
        self.resultCode = resultCode

    def __str__(self):
        if self.message is not None:
            return repr(self.message)
        else:
            return "Empty"

    def __repr__(self):
        return self.__str__()


class MyPlugin(eg.PluginBase):
    def __init__(self):
        self.AddAction(SendMessage)

    def addLine(self, label, control, width=400):
        if (label is not None):
            self.boxsizer.Add(wx.StaticText(self.panel, -1, label + ":"), 0, wx.TOP, 3)
        try:
            control.Size.SetWidth(width)
        except AttributeError:
            print("no Width: " + str(label))
        self.boxsizer.Add(control, 0)
        return control

    def addGroup(self, label):
        sb = wx.StaticBox(self.spanel, label=label)
        self.boxsizer = wx.StaticBoxSizer(sb, wx.VERTICAL)
        self.panel.sizer.Add(self.boxsizer)

    def Configure(self, myport='6868', mykey='1234567890'):
        panel = eg.ConfigPanel(resizable=False)
        self.panel = panel
        self.spanel = ScrollPanel(panel)
        self.spanel.SetupScrolling()
        panel.sizer.Add(self.spanel, 1, wx.ALL | wx.EXPAND)
        self.addGroup("EventGhost Properties")
        # publicIpCtrl = self.addLine("Your Public IP or Host Name (like a dyndns host name). Leave blank to get it automatically", "")
        # textControl = wx.TextCtrl(panel, -1, myport)
        text = 'Please select the port of your choice.\n\nIf it is not working please\nchange the port'
        text = text + ',as the port might\nbe use in another service\n\n\n'
        portCtrl = self.addLine(text + "Port", panel.SpinIntCtrl(myport, min=1, max=65535))
        passwordCtrl = self.addLine("Password", panel.TextCtrl(mykey))
        # panel.sizer.Add(textControl, 1, wx.EXPAND)
        while panel.Affirmed():
            panel.SetResult(portCtrl.GetValue(), passwordCtrl.GetValue())

    def __start__(self, myport, mykey):
        self.key = mykey
        eg.globals.PORT = myport
        print("key:  " + mykey)
        self.port = myport
        self.stopThreadEvent = Event()
        thread = Thread(
            target=self.ThreadLoop,
        )
        thread.start()

    def __stop__(self):
        try:
            self.treadserver.disconnect()
            self.treadserver.stopsock()
        except:
            print('Error in disconnect')
            pass

    def ThreadLoop(self):
        self.treadserver = ThreadedServer('', int(self.port), self.key, self)
        threading.Thread(target=self.treadserver.listen, ).start()

class SendingThread(threading.Thread):
    def __init__(self,init,wheretosend,password):
        super(SendingThread, self).__init__()
        self.init=init
        self.wheretosend=wheretosend
        self.password=password

    def run(self):
        self.init.send(self.wheretosend, self.password)

class SendMessage(eg.ActionBase):
    name = "Send Message"
    description = "Send message to another device...."

    def __call__(self, receiverip, receiverport, password, messageType, isIntent, tag, message, taskname, extra=None,
                 resultCode=RESULT_UNKNOWN,uuidtoadd = ""):
        # saltToAdd,saltToCheck,sender,tag,message,extra,isIntent,mtype,uuidToadd,uuidToCheck,taskName=None,resultCode=RESULT_UNKNOWN
        #print("Hello World!")
        if extra is not None:
            tempextra = extra
            extra = {}
            extra['hash'] = tempextra  # convert to java comopatibkle hash
        wheretosend = IpPort(receiverip, receiverport)
        yourip = IpPort(socket.gethostbyname(socket.gethostname()), eg.globals.PORT)
        uuidToadd = MsgUUID(uuidtoadd)
        uuidToadd.generate()
        message = MyMessage(None, None, yourip, tag, message, extra, isIntent, messageType, uuidToadd, None, taskname,
                            resultCode)
        pending = PendingMessage(message, getCurrentmilli(), wheretosend)
        pending.addToPendings(eg.globals.PENDING_MSG_DATALIST)

        salttoadd = SaltID()
        salttoadd.generate()
        saltobject=SaltObject(getCurrentmilli(),salttoadd.saltString)
        saltobject.addToPendings(eg.globals.SALT_DATA_LIST)
        
        init = MyMessage(salttoadd, None, yourip, None, None, None, False, TYPE_INIT, uuidToadd, None)
        # todo do it in a async Thread
        #init.send(wheretosend, password)
        thread = SendingThread(init,wheretosend, password)
        thread.start()

