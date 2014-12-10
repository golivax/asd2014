package br.usp.ime.lapessc.entity;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class ChangeSet{

	private String commitID;
	private Set<String> changedArtifacts;
	
	public ChangeSet(long commitID, Collection<String> changedArtifacts){
		this(String.valueOf(commitID), changedArtifacts);		
	}
	
	public ChangeSet(String commitID, Collection<String> changedArtifacts){
		this.commitID = commitID;
		this.changedArtifacts = new LinkedHashSet<String>();
		this.changedArtifacts.addAll(changedArtifacts);
	}

	public String getCommitID() {
		return commitID;
	}

	public Set<String> getChangedArtifacts() {
		return changedArtifacts;
	}
	
	public String toString(){
		String s = "Commit: " + commitID + "\n";
		for(String changedArtifact : changedArtifacts){
			s+="- " + changedArtifact + "\n";
		}
		return s;
	}
}