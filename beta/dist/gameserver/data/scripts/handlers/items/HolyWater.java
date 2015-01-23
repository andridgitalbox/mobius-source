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
package handlers.items;

import lineage2.gameserver.model.GameObject;
import lineage2.gameserver.model.Player;
import lineage2.gameserver.model.items.ItemInstance;
import lineage2.gameserver.network.serverpackets.MagicSkillUse;
import lineage2.gameserver.network.serverpackets.SystemMessage;
import lineage2.gameserver.network.serverpackets.components.SystemMessageId;
import npc.model.HellboundRemnantInstance;

/**
 * @author Mobius
 * @version $Revision: 1.0 $
 */
public final class HolyWater extends SimpleItemHandler
{
	private static final int[] ITEM_IDS = new int[]
	{
		9673
	};
	
	/**
	 * Method getItemIds.
	 * @return int[]
	 * @see lineage2.gameserver.handlers.IItemHandler#getItemIds()
	 */
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
	
	/**
	 * Method useItemImpl.
	 * @param player Player
	 * @param item ItemInstance
	 * @param ctrl boolean
	 * @return boolean
	 */
	@Override
	protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl)
	{
		final GameObject target = player.getTarget();
		
		if ((target == null) || !(target instanceof HellboundRemnantInstance))
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET));
			return false;
		}
		
		final HellboundRemnantInstance npc = (HellboundRemnantInstance) target;
		
		if (npc.isDead())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET));
			return false;
		}
		
		player.broadcastPacket(new MagicSkillUse(player, npc, 2358, 1, 0, 0));
		npc.onUseHolyWater(player);
		return true;
	}
}
