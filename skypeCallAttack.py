__author__ = 'ingared'

import sys
import Skype4Py
import time


class SkypeCallAttack:

    skype = Skype4Py.Skype()

    def __init__(self):

        # Attack parameters like number
        count = 0

        # This variable will get its actual value in OnCall handler
        skype = 0
        CallStatus = 0

        # Here we define a set of call statuses that indicate a call has been either aborted or finished
        CallIsFinished = set([Skype4Py.clsFailed, Skype4Py.clsFinished, Skype4Py.clsMissed, Skype4Py.clsRefused, Skype4Py.clsBusy,
             Skype4Py.clsCancelled]);

        CallIsToBeCut = set([Skype4Py.clsEarlyMedia])


        def AttachmentStatusText(status):
            return skype.Convert.AttachmentStatusToText(status)

        def CallStatusText(status):
            return skype.Convert.CallStatusToText(status)

        # This handler is fired when status of Call object has changed
        def OnCall(call, status):
            global CallStatus
            CallStatus = status
            print 'Call status: ' + CallStatusText(status)
            if (status in CallIsToBeCut):
                call.Finish()

        # This handler is fired when Skype attatchment status changes
        def OnAttach(status):
            print 'API attachment status: ' + AttachmentStatusText(status)
            if status == Skype4Py.apiAttachAvailable:
                skype.Attach()

        def OnAttach(status):
            print 'API attachment status: ' + AttachmentStatusText(status)
            if status == Skype4Py.apiAttachAvailable:
                skype.Attach()

         # Creating Skype object and assigning event handlers..
        skype = Skype4Py.Skype()
        skype.OnAttachmentStatus = OnAttach
        skype.OnCallStatus = OnCall
        self.skype = skype

        # Starting Skype if it's not running already..
        if not skype.Client.IsRunning:
            print 'Starting Skype..'
            skype.Client.Start()
        # Attatching to Skype..
        print 'Connecting to Skype..'
        skype.Attach()


        def OnCall(call, status):
            global CallStatus
            CallStatus = status
            print 'Call status: ' + CallStatusText(status)
            if (status in CallIsToBeCut):
                call.Finish()

        # Creating Skype object and assigning event handlers..
        skype.OnCallStatus = OnCall

        # Starting Skype if it's not running already..
        if not skype.Client.IsRunning:
            print 'Starting Skype..'
            skype.Client.Start()

        # Attaching to Skype.
        print 'Connecting to Skype..'
        skype.Attach()


    def skypeAttack(self,mobNumber, count):
        try:
            call =  self.skype.PlaceCall(mobNumber)
            print 'Calling ' + mobNumber + '..' + ' for ' + str(count) + ' time '
            time.sleep(30)
            return count+1
        except:
            pass
            return count