
package org.jenkinsci.plugins.gwt.metadataInGitRepoPlugin.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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
    
    private static final int RETRY_INITIAL_VALUE = 1;
    private static final int MAX_RETRIES_FOR_OPERATION = 5;
    private static final String GIT_REPOSITORY_TAIL = "/.git";
    private static final String GIT_REPOSITORY_LOCAL_PATH = "/tmp/jenkinsMetadataInGitRepo";
    
    private static final Logger LOGGER = Logger.getLogger(GitRepoManager.class.getName());
    private static Object mutex = new Object();
    
    public static String updateLocalRepoIfNeedTo(String repoUrl) {
        synchronized(mutex) {
            String repoPath = GIT_REPOSITORY_LOCAL_PATH;

            GitRepoManager gitRepoManager = new GitRepoManager(repoUrl, repoPath, null, null);
            gitRepoManager.updateLocalRepoIfNeedTo(RETRY_INITIAL_VALUE);

            return repoPath;
        }
    }
    
    private String remotePath;
    private String localPath;
    private Repository localRepo;
    private CredentialsProvider credentialsProvider;
    
    private GitRepoManager(String remotePath, String localPath, String username, String password) {
        
        this.remotePath = remotePath;
        this.localPath = localPath;

        if ((username != null) && (password != null)) {
            credentialsProvider = new UsernamePasswordCredentialsProvider(username, password);
        }
        
    }
    
    private void cloneRepo(int retry) {
        if (retry >= MAX_RETRIES_FOR_OPERATION) {
            return;
        }
        
        try {
            cloneRepoAux();
            return;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "cloneRepo: ", e);
        }
        cloneRepo(retry++);
    }
    
    private void cloneRepoAux() throws IOException, NoFilepatternException, GitAPIException {
        Git.cloneRepository()//.setCredentialsProvider(credentialsProvider)
                .setURI(remotePath)
                .setDirectory(new File(localPath))
                .call();
    }
    
    private void pullFromRepo(int retry) {
        if (retry >= MAX_RETRIES_FOR_OPERATION) {
            return;
        }
        
        try {
            pullFromRepoAux();
            return;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "pullFromRepo: ", e);
        }
        pullFromRepo(retry++);
    }
    
    private void pullFromRepoAux() throws IOException, WrongRepositoryStateException,
            InvalidConfigurationException, DetachedHeadException,
            InvalidRemoteException, CanceledException, RefNotFoundException,
            NoHeadException, GitAPIException {
        
        localRepo = new FileRepository(getFileRepositoryPath());
        Git git = new Git(localRepo);
        git.pull()//.setCredentialsProvider(credentialsProvider)
                .call();
    }
    
    private String getFileRepositoryPath() {
        return localPath + GIT_REPOSITORY_TAIL;
    }
    
    private void updateLocalRepoIfNeedTo(int retry) {
        if (retry >= MAX_RETRIES_FOR_OPERATION) {
            return;
        }
        
        File fileRepository = new File(getFileRepositoryPath());
        if ((fileRepository.exists()) && (fileRepository.isDirectory()) && 
                (fileRepository.canRead()) && (fileRepository.canWrite()) && 
                (fileRepository.canExecute())) {
            pullFromRepo(RETRY_INITIAL_VALUE);
        }
        else {
            removeLocalRepository();
            File parentFolder = new File(localPath);
            if (parentFolder.exists()) {
                updateLocalRepoIfNeedTo(retry++);
            }
            else {
                cloneRepo(RETRY_INITIAL_VALUE);
            }
        }
    }
    
    private void removeLocalRepository() {
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
        }
    }
}
