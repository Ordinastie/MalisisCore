package net.malisis.core.asm;

import java.util.ArrayList;


public class AsmHook
{
	private String targetClass;
	private String targetMethod;
	private String targetMethodObf;
	private String targetMethodDescriptor;
	private ArrayList<AsmInsert> listInserts = new ArrayList<>();
	
	public AsmHook(String targetClass, String methodName, String methodDesc)
	{
		this.targetClass = targetClass; 
		this.targetMethod = methodName;
		this.targetMethodObf = getObfuscatedMethodName(methodName);
		this.targetMethodDescriptor = methodDesc;
	}
	
	public void addInserts(AsmInsert...inserts)
	{
		for(AsmInsert i : inserts)
			listInserts.add(i);
	}
	public ArrayList<AsmInsert> getInserts()
	{
		return listInserts;
	}
		
	public void register()
	{
		MalisisCoreTransformer.register(this.targetClass, this);		
	}
	
	
	private String getObfuscatedMethodName(String methodName)
	{
		return targetMethodObf;
	}

	public String getMethodName()
	{
		return targetMethod;
	}

	public String getMethodDescriptor()
	{
		return targetMethodDescriptor;
	}



}
