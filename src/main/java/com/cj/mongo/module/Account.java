package com.cj.mongo.module;

import java.util.Objects;

public class Account {
	private String id;
	private Integer balance;
	
	public Account(){
		
	}
	public Account(String id, Integer balance) {
		super();
		this.id = id;
		this.balance = balance;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Integer getBalance() {
		return balance;
	}
	public void setBalance(Integer balance) {
		this.balance = balance;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id,balance);
	}

	@Override
	public boolean equals(Object obj) {
		if(this==obj)
			return true;
		if(obj==null || getClass()!=obj.getClass())
			return false;
		Account account=(Account)obj;
		return Objects.equals(id, account.id) && Objects.equals(balance, account.balance);
	}

	@Override
	public String toString() {
		return "Account{id:'"+id+"',balance:"+balance+"}";
	}
	
}
