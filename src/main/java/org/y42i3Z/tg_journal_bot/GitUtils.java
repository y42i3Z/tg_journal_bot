package org.y42i3Z.tg_journal_bot;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.IsoFields;
import java.util.List;
import java.util.Optional;

import static org.y42i3Z.tg_journal_bot.Config.REMOTE_URL;
import static org.y42i3Z.tg_journal_bot.Config.getUserPasswordCredentialsProvider;

@Slf4j
public class GitUtils {
    private static final CredentialsProvider CREDENTIALS_PROVIDER = getUserPasswordCredentialsProvider();

    static void checkoutBranch(Git git, String branchName) throws GitAPIException {
        Ref branch = findBranch(git, branchName)
                .or(() -> createBranch(git, branchName))
                .orElseThrow();
        git.checkout().setName(branch.getName()).call();

        // todo: set upstream
//        branch.
        log.info("Checked out branch '{}'", branch.getName());
    }

    @SneakyThrows
    static Optional<Ref> findBranch(Git git, String branch) {
        log.info("Looking for branch: {}", branch);
        Optional<Ref> found = Optional.empty();
        List<Ref> refs = git.branchList().call();
        for (Ref ref : refs) {
            if (ref.getName().contains(branch)) {
                log.info("Found branch: {}", ref.getName());
                found = Optional.of(ref);
            }
        }
        return found;
    }

    @SneakyThrows
    static Optional<Ref> createBranch(Git git, String branch) {
        log.info("Creating branch: {}", branch);
        return Optional.of(git
                .branchCreate()
                .setName(branch)
                .call());
    }

    static void commitMessage(Git git, String content, Integer date) throws IOException, GitAPIException {
        String fileName = String.format("%02d.md", getCurrWeekNumber());
        String workDir = String.format("%s/%s", git.getRepository().getDirectory().getParent(), getCurrYear());

        File myFile = new File(workDir, fileName);
        FileUtils.writeStringToFile(myFile, content.concat("\n"), "UTF-8", true);

        git.add()
                .addFilepattern(getCurrYear())
                .call();

        RevCommit revCommit = git.commit()
                .setMessage(String.format("Added links from %s", Instant.ofEpochSecond(date).toString()))
                .call();

        log.info("Committed file {} as {} to repository at {}", myFile, revCommit, git.getRepository().getDirectory().getParent());
    }

    static void pushCommittedEntries(Git git, List<Update> updates, String branchName) throws GitAPIException {
        git.push()
                .setRemote(REMOTE_URL)
                .setCredentialsProvider(CREDENTIALS_PROVIDER)
                .setRefSpecs()
                .call();
        git.close();
        log.info("Pushed {} commits into branch {}", updates.size(), branchName);
    }


    static String getBranchName() {
        return String.format("W%02dY%s", getCurrWeekNumber(), getCurrYear());
    }

    static int getCurrWeekNumber() {
        ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"));
        return now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }

    static String getCurrYear() {
        ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"));
        return String.valueOf(now.get(IsoFields.WEEK_BASED_YEAR));
    }
}
