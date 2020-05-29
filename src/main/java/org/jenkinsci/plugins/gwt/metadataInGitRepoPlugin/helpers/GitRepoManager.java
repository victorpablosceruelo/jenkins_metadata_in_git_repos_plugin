
package org.jenkinsci.plugins.gwt.metadataInGitRepoPlugin.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 *
 * @author me
 */
public class GitRepoManager {
    
    private static final int MAX_RETRIES_FOR_OPERATION = 10;
    private static final String GIT_REPOSITORY_TAIL = "/.git";
    public static final String GIT_REPOSITORY_LOCAL_PATH = "/tmp/jenkins_projects_metadata";
    
    private static final Logger LOGGER = Logger.getLogger(GitRepoManager.class.getName());
    private static Object mutex = new Object();
    private static Date lastUpdateTime = null;
    
    public static boolean updateLocalRepoIfNeedTo(String repoUrl, String gitRepoUsername, String gitRepoPassword, OneTimeLogger oneTimeLogger) {
        synchronized(mutex) {
            final String repoPath = GIT_REPOSITORY_LOCAL_PATH;
            boolean updateRepoOk = false;
            
            Date currentDate = new Date();
            if ((lastUpdateTime == null) || (currentDate.after(lastUpdateTime))) {
                
                oneTimeLogger.println("Updating local copy of git repository " + repoPath);
                GitRepoManager gitRepoManager = new GitRepoManager(repoUrl, repoPath, gitRepoUsername, gitRepoPassword, oneTimeLogger);
                updateRepoOk = gitRepoManager.updateLocalRepoIfNeedTo();
            } else {
                oneTimeLogger.println("**NOT** updating local copy of git repository " + repoPath);
                return true;
            }
            
            if (updateRepoOk) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(currentDate);
                calendar.add(Calendar.MINUTE, 2);
                lastUpdateTime = calendar.getTime();
            }
            
            return updateRepoOk;
        }
    }
    
    private OneTimeLogger oneTimeLogger;
    private int currentRetry;
    
    private final String remotePath;
    private final String localPath;
    private Repository localRepo;
    private CredentialsProvider credentialsProvider;
    
    
    private GitRepoManager(final String remotePath, final String localPath, final String gitRepoUsername, final String gitRepoPassword, OneTimeLogger oneTimeLogger) {
        
        this.oneTimeLogger = oneTimeLogger;
        this.currentRetry = 0;
        
        this.remotePath = remotePath;
        this.localPath = localPath;

        if ((gitRepoUsername != null) && (gitRepoPassword != null)) {
            credentialsProvider = new UsernamePasswordCredentialsProvider(gitRepoUsername, gitRepoPassword);
        }
        
    }
    
    private boolean cloneRepo() {
        if (currentRetry >= MAX_RETRIES_FOR_OPERATION) {
            return false;
        }
        
        try {
            cloneRepoAux();
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "cloneRepo: ", e);
            oneTimeLogger.println("Error cloning remote git repository: " + e.getMessage());
            removeLocalRepositoryIfExists();
            return false;
        } catch (GitAPIException e) {
            LOGGER.log(Level.SEVERE, "cloneRepo: ", e);
            oneTimeLogger.println("Error cloning remote git repository: " + e.getMessage());
        }
        currentRetry++;
        return cloneRepo();
    }
    
    private void cloneRepoAux() throws IOException, NoFilepatternException, GitAPIException {
        Git.cloneRepository().setCredentialsProvider(credentialsProvider)
                .setURI(remotePath)
                .setDirectory(new File(localPath))
                .call();
    }
    
    private boolean pullFromRepo() {
        if (currentRetry >= MAX_RETRIES_FOR_OPERATION) {
            return false;
        }
        
        try {
            pullFromRepoAux();
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "pullFromRepo: ", e);
            oneTimeLogger.println("Error pulling from git repository: " + e.getMessage());
            removeLocalRepositoryIfExists();
            return false;
        } catch (GitAPIException e) {
            LOGGER.log(Level.SEVERE, "pullFromRepo: ", e);
            oneTimeLogger.println("Error pulling from git repository: " + e.getMessage());
        }
        currentRetry++;
        return pullFromRepo();
    }
    
    private void pullFromRepoAux() throws IOException, WrongRepositoryStateException,
            InvalidConfigurationException, DetachedHeadException,
            InvalidRemoteException, CanceledException, RefNotFoundException,
            NoHeadException, GitAPIException {
        
        localRepo = new FileRepository(getFileRepositoryPath());
        Git git = new Git(localRepo);
        git.pull().setCredentialsProvider(credentialsProvider)
                .call();
    }
    
    private String getFileRepositoryPath() {
        return localPath + GIT_REPOSITORY_TAIL;
    }
    
    private boolean updateLocalRepoIfNeedTo() {

        boolean opOk = false;
        while (! opOk) {
            
            if (currentRetry >= MAX_RETRIES_FOR_OPERATION) {
                return false;
            }
        
            File fileRepository = new File(getFileRepositoryPath());
            if ((fileRepository.exists()) && (fileRepository.isDirectory()) && 
                    (fileRepository.canRead()) && (fileRepository.canWrite()) && 
                    (fileRepository.canExecute())) {
                opOk = pullFromRepo();
            }
            else {
                removeLocalRepositoryIfExists();

                File localRepoFolderFile = new File(localPath);
                if (! localRepoFolderFile.exists()) {
                    opOk = cloneRepo();
                }
            }
            
            currentRetry++;
        }
        return true;
    }
    
    private void removeLocalRepositoryIfExists() {
        File localRepoFolderFile = new File(localPath);
        if (! localRepoFolderFile.exists()) {
            oneTimeLogger.println("Local copy of git repository does not exist: " + localPath);
            return;
        }

        oneTimeLogger.println("Removing folder: " + localPath);
        try {
            Path directory = Paths.get(localPath);
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
               @Override
               public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                   Files.delete(file);
                   return FileVisitResult.CONTINUE;
               }

               @Override
               public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                   Files.delete(dir);
                   return FileVisitResult.CONTINUE;
               }
            });
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "removeLocalRepository: ", e);
            oneTimeLogger.println("Error removing folder " + localPath);
        }
    }
}
