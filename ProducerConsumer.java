package com.multithreading;

class Resource{
	
	int value = 0;
	boolean available = false;
	
	synchronized void put(int value) throws InterruptedException{
		 
		while(available) {
			wait();
		}
		
		this.value = value;
		available = true;
		
		System.out.println("Producer :"+ value);
		notifyAll();
	}
	
	synchronized int get()throws InterruptedException {
		
		while(!available) {
			wait();
		}
		
		this.value = value;
		available = false;
		
		System.out.println("Consumer :"+value);
		notifyAll();
		
		return value;
	}
}

class Producer implements Runnable{
	
	Resource r;
	Producer(Resource r){
		this.r =  r;
	}
	@Override
	public void run() {
		for(int i=0;i<=5;i++) {
             try {
				r.put(i);
			 } catch (InterruptedException e) {
				e.printStackTrace();
			 }
		}
		
	}
	
}

class Consumer implements Runnable{
	
	Resource r;
	
	Consumer(Resource r){
		this.r = r;
	}
	@Override
	public void run() {
		for(int i=0;i<=5;i++){
			 try {
				r.get();
			 } catch (InterruptedException e) {
				e.printStackTrace();
			 }
		}
		
		
	}
	
}

public class ProducerConsumer {

	public static void main(String[] args) {
		
		Resource r = new Resource();
		Thread producer = new Thread(new Producer(r),"Producer");
		Thread consumer = new Thread(new Consumer(r), "Consumer");
		
		producer.start();
		consumer.start();
		

	}

}
