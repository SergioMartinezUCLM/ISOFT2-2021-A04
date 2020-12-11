package es.uclm.esi.isoft2.a04.General.Domain;

public interface Subject {

	/**
	 * 
	 * @param o
	 */
	void attach(Observer o);

	/**
	 * 
	 * @param o
	 */
	void detach(Observer o);

	// void notify(); => [ERROR] java.lang.Object notify() method is final
	void notifySubject();

}