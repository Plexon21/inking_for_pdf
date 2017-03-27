/****************************************************************************
 *
 * File:            SynchronisedGenericPriorityQueue.cs
 *
 * Description:     The 3-Heights™  Pdf Viewer Control for WPF
 *
 * Copyright:       Copyright (C) 2015 PDF Tools AG, Switzerland
 *                  All rights reserved.
 *                  
 ***************************************************************************/

namespace PdfTools.PdfViewerCSharpAPI.DocumentManagement
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Text;
    using System.Threading;
    using PdfTools.PdfViewerCSharpAPI.DocumentManagement.Requests;

    /// <summary>
    /// A Queue of APdfRequests, Elements will be ordered in the queue according to their Key, firstElement having the highest priority and being popped as first one
    /// </summary>
    public class SynchronisedGenericPriorityQueue : IDisposable 
    {

        private bool abort = false;
        private Object synchronisationLock = new Object();
        QueueElement<IPdfRequest> firstElement;

        private class QueueElement<T>
        {
            public QueueElement(T value, QueueElement<T> next)
            {
                this.value = value;
                this.next = next;
            }
            public T value;
            public QueueElement<T> next;
        }

        public SynchronisedGenericPriorityQueue()
        {
            firstElement = null;
        }

        public void Add(IPdfRequest request, bool overtakeRequestsWithSamePriority = false)
        {
            lock (synchronisationLock)
            {
                if (firstElement == null)
                {
                    firstElement = new QueueElement<IPdfRequest>(request, null);
                }
                else
                {
                    //traverse queue until we reach the location where to insert the request
                    QueueElement<IPdfRequest> element = firstElement;
                    while (element.next != null && (element.next.value.Priority >= request.Priority || ( element.next.value.Priority > request.Priority ) ) )
                        element = element.next;

                    //now insert request in between element and element.next
                    element.next = new QueueElement<IPdfRequest>(request, element.next);
                }
                Monitor.Pulse(synchronisationLock);
            }
        }

        public void CancelRequest(IPdfRequest request)
        {
            lock(synchronisationLock)
            {
                if (firstElement == null)
                {
                    return;
                }
                QueueElement<IPdfRequest> element = firstElement;
                while (element.next != null)
                {
                    if (element.next.value.Equals(request))
                    {
                        element.next = element.next.next;//remove request
                        return;
                    }
                    element = element.next;
                }
            }
        }

        /// <summary>
        /// Pop the first element off the queue (returns and removes from queue)
        /// </summary>
        /// <returns>Value of first element</returns>
        public IPdfRequest Pop()
        {
            IPdfRequest ret;
            lock (synchronisationLock)
            {
                if (firstElement == null)
                {
                    Monitor.Wait(synchronisationLock);
                }
                if (abort)
                    return null;

                ret = firstElement.value;
                firstElement = firstElement.next;
            }
            return ret;
        }

        /// <summary>
        /// replaces an enqueued request with an updated version of that request. Is the old request not found, then the replacement will be added normaly, 
        /// </summary>
        /// <param name="toReplace"></param>
        /// <param name="replacement"></param>
        public void Replace(IPdfRequest toReplace, IPdfRequest replacement)
        {
            lock (synchronisationLock)
            {
                if (firstElement == null || firstElement.value.Priority < replacement.Priority)
                {
                    firstElement = new QueueElement<IPdfRequest>(replacement, firstElement);
                    Monitor.Pulse(synchronisationLock);
                }
                else if (firstElement.value == toReplace)
                {
                    firstElement.value = replacement;
                }
                else
                {
                    QueueElement<IPdfRequest> element = firstElement;
                    while (element.next != null && element.next.value.Priority >= toReplace.Priority && element.next.value != toReplace)
                    {
                        element = element.next;
                    }
                    if (element.next != null && element.next.value == toReplace)
                        element.next.value = replacement;
                    else
                    {
                        element.next = new QueueElement<IPdfRequest>(replacement, element.next);
                        Monitor.Pulse(synchronisationLock);
                    }
                }
            }
        }

        public void Dispose()
        {
            Dispose(true);
        }

        protected virtual void Dispose(bool disposing)
        {
            if (!disposing)
                return;
            lock (synchronisationLock)
            {
                abort = true;
                Monitor.PulseAll(synchronisationLock);
            }
        }

        public override String ToString()
        {
            StringBuilder b = new StringBuilder();
            QueueElement<IPdfRequest> current = firstElement;
            while(current != null)
            {
                b.Append(current.value.ToString());
                current = current.next;
            }
            return b.ToString();
        }
    }
}
