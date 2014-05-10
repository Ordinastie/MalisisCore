package net.malisis.core.asm;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class MalisisCoreTransformer extends MalisisClassTransformer
{
	@Override
	public void registerHooks()
	{
		register(keyboardEventHook());
		register(userAttackEntityEventHook());
	}
	
	public AsmHook userAttackEntityEventHook()
	{
		AsmHook ah = new AsmHook("net.minecraft.client.multiplayer.PlayerControllerMP", "attackEntity", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/Entity;)V");
		
		LabelNode falseLabel = new LabelNode();
		InsnList insert1 = new InsnList();
		insert1.add(new TypeInsnNode(NEW, "net/malisis/core/event/user/UserAttackEvent"));
		insert1.add(new InsnNode(DUP));
		insert1.add(new VarInsnNode(ALOAD, 1));
		insert1.add(new VarInsnNode(ALOAD, 2));
		insert1.add(new MethodInsnNode(INVOKESPECIAL, "net/malisis/core/event/user/UserAttackEvent", "<init>", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/Entity;)V"));
		insert1.add(new MethodInsnNode(INVOKEVIRTUAL, "net/malisis/core/event/user/UserAttackEvent", "post", "()Z"));
		insert1.add(new JumpInsnNode(IFEQ, falseLabel));
		insert1.add(new InsnNode(RETURN));
		insert1.add(falseLabel);
		
		ah.insert(insert1);
		
		return ah;
	}
	
	
	
	public AsmHook keyboardEventHook()
	{
		AsmHook ah = new AsmHook("net.minecraft.client.Minecraft", "runTick", "()V");
		
		LabelNode falseLabel = new LabelNode();
		//if(new KeyboardEvent().post())
		InsnList insert1 = new InsnList();
		insert1.add(new TypeInsnNode(NEW, "net/malisis/core/event/user/KeyboardEvent"));
		insert1.add(new InsnNode(DUP));
		insert1.add(new MethodInsnNode(INVOKESPECIAL, "net/malisis/core/event/user/KeyboardEvent", "<init>", "()V"));
		insert1.add(new MethodInsnNode(INVOKEVIRTUAL, "net/malisis/core/event/user/KeyboardEvent", "post", "()Z"));
		insert1.add(new JumpInsnNode(IFNE, falseLabel));
		
		//L1844: KeyBinding.setKeyBindState(Keyboard.getEventKey(), Keyboard.getEventKeyState());
		InsnList match1 = new InsnList();
		match1.add(new MethodInsnNode(INVOKESTATIC, "org/lwjgl/input/Keyboard", "getEventKey", "()I"));
	    match1.add(new MethodInsnNode(INVOKESTATIC, "org/lwjgl/input/Keyboard", "getEventKeyState", "()Z"));
	    match1.add(new MethodInsnNode(INVOKESTATIC, "net/minecraft/client/settings/KeyBinding", "setKeyBindState", "(IZ)V"));
		

	    //L1973: FMLCommonHandler.instance().fireKeyInput()
		InsnList match2 = new InsnList();
	    match2.add(new MethodInsnNode(INVOKESTATIC, "cpw/mods/fml/common/FMLCommonHandler", "instance", "()Lcpw/mods/fml/common/FMLCommonHandler;"));
	    match2.add(new MethodInsnNode(INVOKEVIRTUAL, "cpw/mods/fml/common/FMLCommonHandler", "fireKeyInput", "()V"));
	    

		
		ah.jumpAfter(match1).insert(insert1).jumpAfter(match2).insert(falseLabel);
		
		return ah;
			
	}
	

}
