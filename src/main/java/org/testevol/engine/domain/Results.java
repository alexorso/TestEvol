package org.testevol.engine.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Results {
	
	private CategoryC1 categoryC1;
	private CategoryC2 categoryC2;
	private CategoryC3 categoryC3;
	private CategoryC4 categoryC4;
	private CategoryC5 categoryC5;
	private CategoryC6 categoryC6;
	private CategoryC7 categoryC7;
	private CategoryC8 categoryC8;
	
	
	
	
	public Results(CategoryC1 categoryC1, CategoryC2 categoryC2,
			CategoryC3 categoryC3, CategoryC4 categoryC4,
			CategoryC5 categoryC5, CategoryC6 categoryC6,
			CategoryC7 categoryC7, CategoryC8 categoryC8) {
		super();
		this.categoryC1 = categoryC1;
		this.categoryC2 = categoryC2;
		this.categoryC3 = categoryC3;
		this.categoryC4 = categoryC4;
		this.categoryC5 = categoryC5;
		this.categoryC6 = categoryC6;
		this.categoryC7 = categoryC7;
		this.categoryC8 = categoryC8;
	}
	public CategoryC1 getCategoryC1() {
		return categoryC1;
	}
	public CategoryC2 getCategoryC2() {
		return categoryC2;
	}
	public CategoryC3 getCategoryC3() {
		return categoryC3;
	}
	public CategoryC4 getCategoryC4() {
		return categoryC4;
	}
	public CategoryC5 getCategoryC5() {
		return categoryC5;
	}
	public CategoryC6 getCategoryC6() {
		return categoryC6;
	}
	public CategoryC7 getCategoryC7() {
		return categoryC7;
	}
	public CategoryC8 getCategoryC8() {
		return categoryC8;
	}
	
	public List<Category> getCategories(){
		List<Category> categories = new ArrayList<Category>();
		categories.add(categoryC1);
		categories.add(categoryC2);
		categories.add(categoryC3);
		categories.add(categoryC4);
		categories.add(categoryC5);
		categories.add(categoryC6);
		categories.add(categoryC7);
		categories.add(categoryC8);
		
		return categories;
	}
	
	public Integer getTotalNumberOfTests(){
		return  categoryC1.getNumberOfTests() + categoryC2.getNumberOfTests() + 
				categoryC3.getNumberOfTests() + categoryC4.getNumberOfTests() + 
				categoryC5.getNumberOfTests() + categoryC6.getNumberOfTests() + 
				categoryC7.getNumberOfTests() + categoryC8.getNumberOfTests();
	}
	
	public Set<String> getAddedMethods(){
		
		Set<String> addedTestMethods = new HashSet<String>();
		
		addedTestMethods .addAll(getCategoryC6().getTestsOnThisCategory());
		addedTestMethods.addAll(getCategoryC7().getTestsOnThisCategory());
		addedTestMethods.addAll(getCategoryC8().getTestsOnThisCategory());
		
		return addedTestMethods;
	}
	
	

}
