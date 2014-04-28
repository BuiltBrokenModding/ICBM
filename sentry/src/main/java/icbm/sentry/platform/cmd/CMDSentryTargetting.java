package icbm.sentry.platform.cmd;

import icbm.sentry.interfaces.IAutoTurret;
import icbm.sentry.interfaces.ITurret;
import icbm.sentry.interfaces.ITurretProvider;
import icbm.sentry.turret.ai.TurretEntitySelector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import calclavia.lib.prefab.terminal.ITerminal;
import calclavia.lib.prefab.terminal.ITerminalCommand;

/** Command set for terminal based sentries to select what target types to shoot at
 * 
 * @author Darkguardsman */
public class CMDSentryTargetting implements ITerminalCommand
{

    @Override
    public List<String> called(EntityPlayer player, ITerminal terminal, String[] args)
    {
        if (args[0].equalsIgnoreCase(this.getCommandName()) && args.length > 1 && args[1] != null)
        {
            String command = args[1];

            if (terminal instanceof ITurretProvider)
            {
                List<String> output_to_console = new ArrayList<String>();
                ITurretProvider provider = (ITurretProvider) terminal;
                ITurret turret = provider.getTurret();

                if (command != null)
                {
                    if (command.equalsIgnoreCase("help"))
                    {
                        output_to_console.add("Listing commands");
                        output_to_console.add("-------------------------------------");
                        output_to_console.add("/target list");
                        output_to_console.add("/target <type> [true/false]");
                        output_to_console.add("/target exact <mob> [true/false]");
                        output_to_console.add("-------------------------------------");
                        return output_to_console;
                    }
                    else if (command.equalsIgnoreCase("target"))
                    {
                        if (turret instanceof IAutoTurret && ((IAutoTurret) turret).getEntitySelector() instanceof TurretEntitySelector)
                        {
                            TurretEntitySelector selector = (TurretEntitySelector) ((IAutoTurret) turret).getEntitySelector();

                            if (args.length > 2 && args[2] != null)
                            {
                                String s = args[2];
                                if (s.equalsIgnoreCase("list"))
                                {
                                    //TODO add page selector if this list gets to long
                                    output_to_console.add("Listing target types");
                                    output_to_console.add("-------------------------------------");
                                    for (Entry<String, Boolean> entry : selector.targetting.entrySet())
                                    {
                                        output_to_console.add("Target: " + entry.getKey() + " = " + entry.getValue());
                                    }
                                    output_to_console.add("-------------------------------------");
                                }
                                else if (s.equalsIgnoreCase("exact"))
                                {
                                    if (args.length > 3 && args[3] != null)
                                    {
                                        String t = args[3];
                                        boolean bool = selector.canTargetType(t);
                                        if (args.length > 4 && args[4] != null)
                                        {
                                            String b = args[4];
                                            if (b.equalsIgnoreCase("true") || b.equalsIgnoreCase("t"))
                                            {
                                                bool = true;
                                            }
                                            else
                                            {
                                                bool = false;
                                            }
                                        }
                                        else
                                        {
                                            bool = !bool;
                                        }
                                        selector.setTargetType(t, bool);
                                        output_to_console.add("Set " + t + " to " + bool);
                                    }
                                    else
                                    {
                                        output_to_console.add("Missing type arg");
                                    }
                                }
                                else if (selector.targetting.containsKey(args[2]))
                                {
                                    String t = args[2];
                                    boolean bool = selector.canTargetType(t);
                                    if (args.length > 3 && args[3] != null)
                                    {
                                        String b = args[3];
                                        if (b.equalsIgnoreCase("true") || b.equalsIgnoreCase("t"))
                                        {
                                            bool = true;
                                        }
                                        else
                                        {
                                            bool = false;
                                        }
                                    }
                                    else
                                    {
                                        bool = !bool;
                                    }
                                    selector.setTargetType(t, bool);
                                    output_to_console.add("Set " + t + " to " + bool);
                                }
                                else
                                {
                                    output_to_console.add("Missing type args");
                                }
                            }
                            else
                            {
                                output_to_console.add("Missing args");
                            }
                        }
                        else
                        {
                            output_to_console.add("Unsupported command for this sentry");
                        }
                        return output_to_console;
                    }
                }
            }

        }
        return null;
    }

    @Override
    public boolean canSupport(ITerminal terminal)
    {
        return terminal instanceof ITurretProvider;
    }

    @Override
    public String getCommandName()
    {
        return "target";
    }

    @Override
    public Set<String> getPermissionNodes()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getNode(String[] args)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
