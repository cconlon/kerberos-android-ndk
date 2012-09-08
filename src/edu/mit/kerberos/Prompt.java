package edu.mit.kerberos;

public class Prompt
{
	String prompt;

	public String getPrompt()
	{
		return prompt;
	}

	boolean isHidden;

	public boolean isHidden()
	{
		return isHidden;
	}

	public Prompt(String prompt, boolean isHidden)
	{
		this.prompt = prompt;
		this.isHidden = isHidden;
	}
}
