package net.runelite.client.plugins.itemiconexport;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.inject.Provides;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Sends data to a backend which then relays it further to frontend
 * which displays live data of the player
 */
@PluginDescriptor(
        name = "Item Icon Export",
        description = "Export all item icons to .png",
        tags = {"item", "icon", "export"},
        enabledByDefault = true
)
@Getter
public class ItemIconExportPlugin extends Plugin implements ActionListener
{
    public JButton exportButton;
    private NavigationButton navButton;

    @Inject
    private Client client;

    @Inject
    private ItemManager itemManager;

    @Inject
    private ClientThread clientThread;

    @Inject
    private ConfigManager configManager;

    @Inject
    private ItemIconExportConfig config;

    @Inject
    private ClientToolbar clientToolbar;

    @Provides
    private ItemIconExportConfig getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(ItemIconExportConfig.class);
    }

    public void exportIcons()
    {
        clientThread.invokeLater(() ->
        {
            System.out.println("Exporting item compositions...");
            int forceStop = 26156; // idk sometimes it returns a random dwarf for any ID past max item id, so force stop at known value
            int itemId = 0;
            String outPath = config.exportPath();

            ItemComposition itemComp = null;

            JsonObject jsonObject = new JsonObject();
            JsonArray jsonArr = new JsonArray();

            do {
                itemComp = itemManager.getItemComposition(itemId);
                if(itemComp == null)
                    break;

                JsonObject jsonItemObj = new JsonObject();
                jsonItemObj.addProperty("id", itemId);
                jsonItemObj.addProperty("name", itemComp.getName());

                jsonArr.add(jsonItemObj);
                try {
                    String imgPath = outPath + "\\" + itemId + ".png";
                    File output = new File(imgPath);
                    BufferedImage icon = itemManager.getImage(itemId);
                    ImageIO.write(icon, "png", output);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                itemId++;
            }while(itemId < forceStop);
            jsonObject.add("items", jsonArr);

            String jsonPath = outPath + "\\" + "itemcomp.json";
            try (FileWriter file = new FileWriter(jsonPath))
            {
                file.write(jsonObject.toString());
                file.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Finished item compositions!");
        });
    }

    public void actionPerformed(ActionEvent arg0)
    {
        JButton btn = (JButton)arg0.getSource();

        if(btn == exportButton)
        {
            // debug stuff
            exportIcons();
        }
    }

    @Override
    protected void startUp() throws Exception
    {
        exportButton = new JButton("Export");
        exportButton.addActionListener(this);


        final ItemIconExportPanel panel = injector.getInstance(ItemIconExportPanel.class);

        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "itemiconexport.png");

        navButton = NavigationButton.builder()
                .tooltip("Item Icon Export")
                .icon(icon)
                .priority(1)
                .panel(panel)
                .build();

        clientToolbar.addNavigation(navButton);
    }

    @Override
    protected void shutDown() throws Exception
    {
        clientToolbar.removeNavigation(navButton);
    }
}