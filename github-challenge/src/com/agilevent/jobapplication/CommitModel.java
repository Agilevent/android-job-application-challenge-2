package com.agilevent.jobapplication;

import java.util.ArrayList;
import java.util.List;

public class CommitModel {

	public CommitModel() {
		commits = new ArrayList<Commit>(); 
	}
	
	public List<Commit> commits; 
	
	public class Commit {
		
		public Commit() {
			author = new Author(); 
		}
		
		public String message; 
		public String id; 
		public Author author; 
		
		public class Author {
			public String name; 
		}
	}
}
