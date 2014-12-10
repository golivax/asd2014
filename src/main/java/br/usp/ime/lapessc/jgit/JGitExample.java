package br.usp.ime.lapessc.jgit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import br.usp.ime.lapessc.entity.ChangeSet;

public class JGitExample {

	private Git git;
	
	public JGitExample(){

	}

	public Set<ChangeSet> mineChangeSets(String url, String cloneDir, 
			String startCommitID, String endCommitID){

		Set<ChangeSet> changeSets = new LinkedHashSet<ChangeSet>() ;
		
		try{
			
			//Cloning the repo
			this.git = Git.cloneRepository().setURI(url).
					setDirectory(new File(cloneDir)).call();
			
			//To open an existing repo
			//this.git = Git.open(new File(cloneDir));
			
			List<RevCommit> commits = getCommitsInRange(
					startCommitID, endCommitID);

			changeSets = extractChangeSets(commits);

		}catch(Exception e){
			e.printStackTrace();
		}

		return changeSets;

	}

	private Set<ChangeSet> extractChangeSets(List<RevCommit> commits) throws 
		MissingObjectException,	IncorrectObjectTypeException, 
		CorruptObjectException, IOException {
		
		Set<ChangeSet> changeSets = new LinkedHashSet<ChangeSet>();
		
		for(int i = 0; i < commits.size() - 1; i++){
			RevCommit commit = commits.get(i);
			RevCommit parentCommit = commits.get(i+1);
			ChangeSet changeSet = getChangeSet(commit, parentCommit);
			changeSets.add(changeSet);
		}
		
		//If startCommit is the first commit in repo, then we
		//need to do something different to get the changeset
		RevCommit startCommit = commits.get(commits.size()-1);
		if(startCommit.getParentCount() == 0){
			ChangeSet changeSet = getChangeSetForFirstCommit(startCommit);
			changeSets.add(changeSet);
		}		
		
		return changeSets;
	}
	
	private ChangeSet getChangeSetForFirstCommit(RevCommit startCommit) throws 
		MissingObjectException, IncorrectObjectTypeException, 
		CorruptObjectException, IOException {

		Set<String> changedArtifacts = new HashSet<String>();

		Repository repo = git.getRepository();
		TreeWalk tw = new TreeWalk(repo);
		tw.reset();
		tw.setRecursive(true);
		tw.addTree(startCommit.getTree());

		while(tw.next()){
			changedArtifacts.add(tw.getPathString());
		}

		ChangeSet changeSet = new ChangeSet(
				startCommit.getName(), changedArtifacts);
		
		return changeSet;
	}

	private ChangeSet getChangeSet(RevCommit commit, RevCommit parentCommit) 
			throws IOException {
	
		Repository repository = git.getRepository();
		
		DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
		df.setRepository(repository);
		df.setDiffComparator(RawTextComparator.DEFAULT);
		df.setDetectRenames(true);
		
		List<DiffEntry> diffs = df.scan(
				parentCommit.getTree(), commit.getTree());
		
		Set<String> changedArtifacts = new HashSet<String>();
		for (DiffEntry diff : diffs){

			String path;
			if(ChangeType.DELETE.equals(diff.getChangeType())){
				path = diff.getOldPath();
			}
			else{
				path = diff.getNewPath();
			}
			
		    changedArtifacts.add(path);
		}
		
		ChangeSet changeSet = new ChangeSet(commit.getName(),changedArtifacts);
		return changeSet;
	}

	//Equivalent to "git log startCommitID^^..endCommitID --first-parent" 
	private List<RevCommit> getCommitsInRange(
			String startCommitID, String endCommitID) throws 
				RevisionSyntaxException, AmbiguousObjectException, IOException{
		
		List<RevCommit> commits = new ArrayList<RevCommit>();
		Repository repo = git.getRepository();
		
		RevWalk rw = new RevWalk(repo);
		rw.setRetainBody(false);

		RevCommit startCommit = rw.parseCommit(repo.resolve(startCommitID));
		RevCommit commit = rw.parseCommit(repo.resolve(endCommitID));		
		
		commits.add(commit);
		
		while(!commit.equals(startCommit)){
			
			RevCommit parentCommit = rw.parseCommit(commit.getParent(0));
			commits.add(parentCommit);
			commit = parentCommit;
		}
		
		//if startCommit is NOT the firstCommit in repo
		if(startCommit.getParentCount() > 0){
			RevCommit parentCommit = rw.parseCommit(commit.getParent(0));
			commits.add(parentCommit);
		}
		
		rw.dispose();
		
		return commits;
	}

	public static void main(String[] args) {
		String url = "https://github.com/golivax/JDX.git";
		String cloneDir = "c:/tmp/jdx";
		String startCommit = "ca44b718d43623554e6b890f2895cc80a2a0988f";
		String endCommit = "9379963ac0ded26db6c859f1cc001f4a2f26bed1";

		JGitExample jGitExample = new JGitExample();

		Set<ChangeSet> changeSets = 
				jGitExample.mineChangeSets(url,cloneDir,startCommit, endCommit);

		for(ChangeSet changeSet : changeSets){
			System.out.println(changeSet);	
		}
	}

}