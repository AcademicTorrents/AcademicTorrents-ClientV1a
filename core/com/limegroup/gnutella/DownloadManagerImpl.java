package com.limegroup.gnutella;

import java.io.File;
import java.net.Socket;
import java.util.Collection;
import java.util.List;

import org.gudy.azureus2.core3.global.GlobalManager;

import com.frostwire.AzureusStarter;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.limegroup.gnutella.settings.SharingSettings;
import com.limegroup.gnutella.settings.UpdateSettings;
import com.limegroup.gnutella.version.DownloadInformation;

@Singleton
public class DownloadManagerImpl implements DownloadManager {
    
    /**
     * The average bandwidth over all downloads.
     * This is only counted while downloads are active.
     */
    private float averageBandwidth = 0;
    
    private final Provider<DownloadCallback> downloadCallback;
    
    @Inject
    public DownloadManagerImpl(
            Provider<DownloadCallback> downloadCallback) {
        this.downloadCallback = downloadCallback;
    }


    //////////////////////// Creation and Saving /////////////////////////

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#initialize()
     */
    public void initialize() {
        //scheduleWaitingPump();
    }
    
    private void addDownloaderManager(org.gudy.azureus2.core3.download.DownloadManager downloader) {
        synchronized(this) {
            callback(downloader).addDownloadManager(downloader);
        }
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#postGuiInit()
     */
    public void loadSavedDownloadsAndScheduleWriting() {
        loadTorrentDownloads();
        //loadSavedDownloads();
        //scheduleSnapshots();
    }
    
    /**
     * This is where torrents are loaded from the last session.
     * If seeding is not enaebled, completed torrents won't be started, they'll be stopped.
     */
    private void loadTorrentDownloads() {
        GlobalManager globalManager = AzureusStarter.getAzureusCore().getGlobalManager();
        List<?> downloadManagers = globalManager.getDownloadManagers();
        for (Object obj : downloadManagers) {
            if (obj instanceof org.gudy.azureus2.core3.download.DownloadManager) {

            	org.gudy.azureus2.core3.download.DownloadManager downloadManager = (org.gudy.azureus2.core3.download.DownloadManager) obj;
            	
            	if (downloadManager.getSaveLocation().getParentFile().getAbsolutePath().equals(UpdateSettings.UPDATES_DIR.getAbsolutePath())) {
            	    continue;
            	}
                
            	if (!SharingSettings.SEED_FINISHED_TORRENTS.getValue()) {
            		if (downloadManager.getAssumedComplete()) {
            		    downloadManager.pause();
            		}
            	}

                addDownloaderManager(downloadManager);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#killDownloadersNotListed(java.util.Collection)
     */
    public synchronized void killDownloadersNotListed(Collection<? extends DownloadInformation> updates) {
    }

    /**
     * Delegates the incoming socket out to BrowseHostHandler & then attempts to assign it
     * to any ManagedDownloader.
     * 
     * Closes the socket if neither BrowseHostHandler nor any ManagedDownloaders wanted it.
     * 
     * @param file
     * @param index
     * @param clientGUID
     * @param socket
     */
    private synchronized boolean handleIncomingPush(String file, int index, byte [] clientGUID, Socket socket) {
         return false;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#acceptPushedSocket(java.lang.String, int, byte[], java.net.Socket)
     */
    public boolean acceptPushedSocket(String file, int index,
            byte[] clientGUID, Socket socket) {
        return handleIncomingPush(file, index, clientGUID, socket);
    }
    
    public boolean allowNewTorrents() {
    	return true;
    	
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#isIncomplete(com.limegroup.gnutella.URN)
     */
    public boolean isIncomplete(URN urn) {
        return false;//incompleteFileManager.getFileForUrn(urn) != null;
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#isActivelyDownloading(com.limegroup.gnutella.URN)
     */
    public boolean isActivelyDownloading(URN urn) {
        Downloader md = getDownloaderForURN(urn);
        
        if(md == null)
            return false;
            
        switch(md.getState()) {
        case QUEUED:
        case BUSY:
        case ABORTED:
        case GAVE_UP:
        case DISK_PROBLEM:
        case CORRUPT_FILE:
        case REMOTE_QUEUED:
        case WAITING_FOR_USER:
            return false;
        default:
            return true;
        }
    }  
 
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#downloadsInProgress()
     */
    public synchronized int downloadsInProgress() {
        return 0;
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#getNumIndividualDownloaders()
     */
    public synchronized int getNumIndividualDownloaders() {
        return 0;
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#getNumActiveDownloads()
     */
    public synchronized int getNumActiveDownloads() {
        return 0;//active.size() - innetworkCount;
    }
   
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#getNum<Downloads()
     */
    public synchronized int getNumWaitingDownloads() {
        return 0;//waiting.size();
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#getDownloaderForURN(com.limegroup.gnutella.URN)
     */
    public synchronized Downloader getDownloaderForURN(URN sha1) {
        return null;
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#getDownloaderForURNString(java.lang.String)
     */
    public synchronized Downloader getDownloaderForURNString(String urn) {
        return null;
    }    
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#getDownloaderForIncompleteFile(java.io.File)
     */
    public synchronized Downloader getDownloaderForIncompleteFile(File file) {
        return null;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#isGuidForQueryDownloading(com.limegroup.gnutella.GUID)
     */
    public synchronized boolean isGuidForQueryDownloading(GUID guid) {
        return false;
    }
    
    void clearAllDownloads() {
    }
    
    private DownloadCallback callback(org.gudy.azureus2.core3.download.DownloadManager dm) {
        return downloadCallback.get();
    }
        
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#conflicts(com.limegroup.gnutella.URN, long, java.io.File)
     */
    public boolean conflicts(URN urn, long fileSize, File... fileName) {
        return false;
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#isSaveLocationTaken(java.io.File)
     */
    public synchronized boolean isSaveLocationTaken(File candidateFile) {
        return false;
    }
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#bumpPriority(com.limegroup.gnutella.Downloader, boolean, int)
     */
    public synchronized void bumpPriority(Downloader downl,
                                          boolean up, int amt) {
     
    }


    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#measureBandwidth()
     */
    public void measureBandwidth() {
     
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#getMeasuredBandwidth()
     */
    public float getMeasuredBandwidth() {
        return 0;
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#getAverageBandwidth()
     */
    public synchronized float getAverageBandwidth() {
        return averageBandwidth;
    }
}
