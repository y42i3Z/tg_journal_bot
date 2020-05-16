package org.y42i3Z.tg_journal_bot;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;

@Getter
@Slf4j
@Component
public class DisposableGitWrapper {

    private final File localPath;
    private final Git git;
    private final String remoteUri = Config.REMOTE_URL;

    public DisposableGitWrapper(CredentialsProvider credentialsProvider, ProgressMonitor progressMonitor) throws GitAPIException, IOException {
        localPath = File.createTempFile("GitRepository", "");
        if (!localPath.delete()) {
            throw new IOException("Could not delete temporary file " + localPath);
        }
        git = Git.cloneRepository()
                .setURI(remoteUri)
                .setDirectory(localPath)
                .setCredentialsProvider(credentialsProvider)
                .setProgressMonitor(progressMonitor)
                .call();

        git.pull()
//                .setRemote(remoteUri)
                .setCredentialsProvider(credentialsProvider)
                .setProgressMonitor(progressMonitor)
                .call();

//        Repository repository = FileRepositoryBuilder.create(new File(localPath + "/.git"));
//        git = Git.open(repository.getDirectory());
    }

    @PreDestroy
    public void destroy() throws IOException {
        FileUtils.deleteDirectory(localPath);
        log.info("Temporary file has been destroyed");
    }
}
