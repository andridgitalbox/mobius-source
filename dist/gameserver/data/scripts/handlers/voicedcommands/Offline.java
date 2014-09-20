/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.voicedcommands;

import lineage2.gameserver.Config;
import lineage2.gameserver.handlers.IVoicedCommandHandler;
import lineage2.gameserver.handlers.VoicedCommandHandler;
import lineage2.gameserver.model.Player;
import lineage2.gameserver.model.Zone;
import lineage2.gameserver.model.entity.olympiad.Olympiad;
import lineage2.gameserver.network.serverpackets.components.CustomMessage;
import lineage2.gameserver.scripts.Functions;
import lineage2.gameserver.scripts.ScriptFile;

/**
 * @author Mobius
 * @version $Revision: 1.0 $
 */
public class Offline extends Functions implements IVoicedCommandHandler, ScriptFile
{
	private final String[] _commandList = new String[]
	{
		"offline"
	};
	
	/**
	 * Method useVoicedCommand.
	 * @param command String
	 * @param activeChar Player
	 * @param args String
	 * @return boolean
	 * @see lineage2.gameserver.handlers.IVoicedCommandHandler#useVoicedCommand(String, Player, String)
	 */
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if (!Config.SERVICES_OFFLINE_TRADE_ALLOW)
		{
			return false;
		}
		
		if ((activeChar.getOlympiadObserveGame() != null) || (activeChar.getOlympiadGame() != null) || Olympiad.isRegisteredInComp(activeChar))
		{
			activeChar.sendActionFailed();
			return false;
		}
		
		if (activeChar.getLevel() < Config.SERVICES_OFFLINE_TRADE_MIN_LEVEL)
		{
			show(new CustomMessage("voicedcommandhandlers.Offline.LowLevel", activeChar).addNumber(Config.SERVICES_OFFLINE_TRADE_MIN_LEVEL), activeChar);
			return false;
		}
		
		if (!activeChar.isInZone(Zone.ZoneType.offshore) && Config.SERVICES_OFFLINE_TRADE_ALLOW_OFFSHORE)
		{
			show(new CustomMessage("trade.OfflineNoTradeZoneOnlyOffshore", activeChar), activeChar);
			return false;
		}
		
		if (!activeChar.isInStoreMode())
		{
			show(new CustomMessage("voicedcommandhandlers.Offline.IncorrectUse", activeChar), activeChar);
			return false;
		}
		
		if (activeChar.getNoChannelRemained() > 0)
		{
			show(new CustomMessage("voicedcommandhandlers.Offline.BanChat", activeChar), activeChar);
			return false;
		}
		
		if (activeChar.isActionBlocked(Zone.BLOCKED_ACTION_PRIVATE_STORE))
		{
			show(new CustomMessage("trade.OfflineNoTradeZone", activeChar), activeChar);
			return false;
		}
		
		if ((Config.SERVICES_OFFLINE_TRADE_PRICE > 0) && (Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM > 0))
		{
			if (getItemCount(activeChar, Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM) < Config.SERVICES_OFFLINE_TRADE_PRICE)
			{
				show(new CustomMessage("voicedcommandhandlers.Offline.NotEnough", activeChar).addItemName(Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM).addNumber(Config.SERVICES_OFFLINE_TRADE_PRICE), activeChar);
				return false;
			}
			
			removeItem(activeChar, Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM, Config.SERVICES_OFFLINE_TRADE_PRICE);
		}
		
		activeChar.offline();
		return true;
	}
	
	/**
	 * Method getVoicedCommandList.
	 * @return String[]
	 * @see lineage2.gameserver.handlers.IVoicedCommandHandler#getVoicedCommandList()
	 */
	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
	
	/**
	 * Method onLoad.
	 * @see lineage2.gameserver.scripts.ScriptFile#onLoad()
	 */
	@Override
	public void onLoad()
	{
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
	}
	
	/**
	 * Method onReload.
	 * @see lineage2.gameserver.scripts.ScriptFile#onReload()
	 */
	@Override
	public void onReload()
	{
	}
	
	/**
	 * Method onShutdown.
	 * @see lineage2.gameserver.scripts.ScriptFile#onShutdown()
	 */
	@Override
	public void onShutdown()
	{
	}
}