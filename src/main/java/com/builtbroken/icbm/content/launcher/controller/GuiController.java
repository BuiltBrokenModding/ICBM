package com.builtbroken.icbm.content.launcher.controller;

import com.builtbroken.icbm.content.launcher.TileAbstractLauncher;
import com.builtbroken.mc.core.References;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.gui.ContainerDummy;
import com.builtbroken.mc.prefab.gui.GuiContainerBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

/** Main GUI for the small missile silo controller
 * Created by robert on 2/20/2015.
 */
public class GuiController extends GuiContainerBase
{
    protected TileController controller;

    GuiButton[] buttons;

    protected GuiTextField x_field;
    protected GuiTextField y_field;
    protected GuiTextField z_field;
    protected String errorString = "";

    boolean editMode = false;
    int editMissile = 0;
    long lastClickTime = 0;

    private int ticks = 0;
    private static int updateGuiTicks = 100;


    public GuiController(TileController launcher, EntityPlayer player)
    {
        super(new ContainerDummy(player, launcher));
        this.controller = launcher;
        this.baseTexture = References.GUI__MC_EMPTY_FILE;
    }

    @Override
    public void initGui()
    {
        super.initGui();

        int x = this.guiLeft + 20;
        int y = this.guiTop + 20;

        if (!editMode)
        {
            if (controller.launcherData != null)
            {
                for (int i = 0; i < controller.launcherData.size(); i++)
                {
                    buttons = new GuiButton[controller.launcherData.size() * 2];

                    //Launcher edit button
                    buttons[i] = new GuiButton(i, x, y, 100, 20, LanguageUtility.getLocalName("gui.icbm:controller.launcher") + " " + i);
                    this.buttonList.add(buttons[i]);

                    //Fire launcher button
                    buttons[i + controller.launcherData.size()] = new GuiButton(i + controller.launcherData.size(), x + 105, y, 30, 20, LanguageUtility.getLocalName("gui.icbm:controller.fire"));
                    if (controller.launcherData.get(i).missile == null)
                        buttons[i + controller.launcherData.size()].enabled = false;
                    this.buttonList.add(buttons[i + controller.launcherData.size()]);
                    y += 22;
                }
            }
        } else
        {
            this.buttonList.add(new GuiButton(0, x + 65, y + 35, 30, 20, LanguageUtility.getLocalName("gui.icbm:controller.back")));
            this.buttonList.add(new GuiButton(1, x + 100, y + 35, 50, 20, LanguageUtility.getLocalName("gui.icbm:controller.update")));

            x = guiLeft + 10;
            y = guiTop + 25;

            TileEntity tile = controller.launcherData.get(editMissile).location.getTileEntity();

            if (tile instanceof TileAbstractLauncher && ((TileAbstractLauncher) tile).target != null)
            {
                this.x_field = this.newField(x, y, 40, 20, "" + ((TileAbstractLauncher) tile).target.xi());
                this.y_field = this.newField(x + 45, y, 40, 20, "" + ((TileAbstractLauncher) tile).target.yi());
                this.z_field = this.newField(x + 90, y, 40, 20, "" + ((TileAbstractLauncher) tile).target.zi());
            }
        }
    }

    public void reloadData()
    {
        if (!editMode)
        {
            initGui();
        }
        else if(this.x_field != null && this.y_field != null && this.z_field != null && this.x_field.isFocused() && this.y_field.isFocused() && this.z_field.isFocused())
        {
            TileEntity tile = controller.launcherData.get(editMissile).location.getTileEntity();

            if (tile instanceof TileAbstractLauncher && ((TileAbstractLauncher) tile).target != null)
            {
                this.x_field.setText("" + ((TileAbstractLauncher) tile).target.xi());
                this.y_field.setText("" + ((TileAbstractLauncher) tile).target.yi());
                this.z_field.setText("" + ((TileAbstractLauncher) tile).target.zi());
            }
        }
    }


    @Override
    protected void actionPerformed(GuiButton button)
    {
        super.actionPerformed(button);
        //Prevents double click when GUI is reloaded
        if(Minecraft.getSystemTime() - lastClickTime < 2) return;
        this.errorString = "";

        if (!editMode)
        {
            if (button.id >= 0 && button.id < buttons.length)
            {
                //Edit launcher button
                if (button.id < controller.launcherData.size())
                {
                    editMissile = button.id;
                    editMode = true;
                    initGui();
                }
                //Fire launcher
                else
                {
                    controller.fireLauncher(button.id - controller.launcherData.size());
                }
            }
        } else
        {
            if (button.id == 0)
            {
                editMissile = -1;
                editMode = false;
                initGui();

            } else if (button.id == 1)
            {
                TileEntity tile = controller.launcherData.get(editMissile).location.getTileEntity();
                if (tile instanceof TileAbstractLauncher)
                {
                    try
                    {
                        ((TileAbstractLauncher) tile).setTarget(new Pos(Integer.parseInt(x_field.getText()), Integer.parseInt(y_field.getText()), Integer.parseInt(z_field.getText())));
                    } catch (NumberFormatException e)
                    {
                        errorString = "gui.icbm:controller.invalid.input";
                    }
                }
            }
        }

        lastClickTime = Minecraft.getSystemTime();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        if(ticks++ >= updateGuiTicks)
        {
            reloadData();
        }
        drawStringCentered(LanguageUtility.getLocalName(controller.getInventoryName()), 85, 10);

        if (!editMode && (controller.launcherData == null || controller.launcherData.size() == 0))
        {
            drawStringCentered(LanguageUtility.getLocal("gui.icbm:controller.links.none"), 85, 40);
            drawStringCentered(LanguageUtility.getLocal("gui.icbm:controller.links.none.hint"), 85, 50);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
    }
}
