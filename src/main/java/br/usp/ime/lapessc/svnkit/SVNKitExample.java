package br.usp.ime.lapessc.svnkit;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import br.usp.ime.lapessc.entity.ChangeSet;

public class SVNKitExample {

	public SVNKitExample(){
		
	}
	
	public Set<ChangeSet> mineChangeSets(String url, String name, 
			String password, int startRevision, int endRevision){

		Set<ChangeSet> changeSets = new LinkedHashSet<ChangeSet>();

		DAVRepositoryFactory.setup();
		try {
			SVNRepository repository = SVNRepositoryFactory.create(
					SVNURL.parseURIEncoded(url));
			
			ISVNAuthenticationManager authManager = 
					SVNWCUtil.createDefaultAuthenticationManager(
							name, password);
			
			repository.setAuthenticationManager(authManager);

			Collection<SVNLogEntry> logEntries = repository.log(
					new String[] {""} , null, startRevision, endRevision, 
					true , true);
			
			for(SVNLogEntry logEntry : logEntries){			
				
				Map<String,SVNLogEntryPath> changedPathsMap = 
						logEntry.getChangedPaths();
				
				if (!changedPathsMap.isEmpty()) {
					
					long revision = logEntry.getRevision();
					Set<String> changedPaths = 
							logEntry.getChangedPaths().keySet();
					
					ChangeSet changeSet = new ChangeSet(revision, changedPaths);
					changeSets.add(changeSet);
				}
			}
			
		}catch(SVNException e){
			e.printStackTrace();
		}
		
		return changeSets;
	}

	public static void main(String[] args) {

		String url = "http://svn.code.sf.net/p/moenia/code/trunk";
		String name = "anonymous";
		String password = "anonymous";
		int startRev = 0;
		int endRev = -1; //HEAD (the latest) revision
		
		SVNKitExample svnKitExample = new SVNKitExample();
		
		Set<ChangeSet> changeSets = 
				svnKitExample.mineChangeSets(url, name, password, 
						startRev, endRev);
		
		//Printing change-sets
		for(ChangeSet changeSet : changeSets){
			System.out.println(changeSet);
		}
		
	}
	
	public String toCSV(Set<ChangeSet> changeSets){
		String csv = new String();
		for(ChangeSet changeSet : changeSets){
			
			String cs = new String();
			for(String artifact : changeSet.getChangedArtifacts()){
				if(artifact.endsWith(".java")){
					cs+=artifact + ",";	
				}				
			}
			cs = StringUtils.removeEnd(cs, ",");
			if(!cs.isEmpty()){
				csv+=cs + "\n";	
			}
			
		}
		
		return csv;
	}
	
}