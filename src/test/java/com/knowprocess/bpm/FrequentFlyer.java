package com.knowprocess.bpm;

public class FrequentFlyer {

	private int km;

	public FrequentFlyer flies(int distance) {
		return null;
	}

	public int getBalance() {
		return km;
	}

	public int kilometers() {
		return km;
	}

	public static FrequentFlyer withInitialBalanceOf(int initialBalance) {
		FrequentFlyer flyer = new FrequentFlyer();
		flyer.km = initialBalance;
		return flyer;
	}

	public Status getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

}
