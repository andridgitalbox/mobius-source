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

import lineage2.gameserver.model.Playable;
import lineage2.gameserver.model.Player;
import lineage2.gameserver.model.Summon;
import lineage2.gameserver.model.items.ItemInstance;
import lineage2.gameserver.network.serverpackets.MagicSkillUse;
import lineage2.gameserver.network.serverpackets.SystemMessage;
import lineage2.gameserver.network.serverpackets.components.SystemMessageId;

/**
 * @author Mobius
 * @version $Revision: 1.0 $
 */
public final class BeastShot extends ScriptItemHandler
{
	private final static int[] _itemIds =
	{
		6645,
		6646,
		6647,
		20332,
		20333,
		20334
	};
	
	/**
	 * Method useItem.
	 * @param playable Playable
	 * @param item ItemInstance
	 * @param ctrl boolean
	 * @return boolean
	 * @see lineage2.gameserver.handlers.IItemHandler#useItem(Playable, ItemInstance, boolean)
	 */
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if ((playable == null) || !playable.isPlayer())
		{
			return false;
		}
		
		final Player player = (Player) playable;
		boolean isAutoSoulShot = false;
		
		if (player.getAutoSoulShot().contains(item.getId()))
		{
			isAutoSoulShot = true;
		}
		
		if (player.getSummonList().size() == 0)
		{
			if (!isAutoSoulShot)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PETS_AND_SERVITORS_ARE_NOT_AVAILABLE_AT_THIS_TIME));
			}
			
			return false;
		}
		
		int deadCount = 0;
		
		for (Summon summon : player.getSummonList())
		{
			if (summon.isDead())
			{
				deadCount++;
			}
		}
		
		if (deadCount == player.getSummonList().size())
		{
			if (!isAutoSoulShot)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SOULSHOTS_AND_SPIRITSHOTS_ARE_NOT_AVAILABLE_FOR_A_DEAD_PET_OR_SERVITOR_SAD_ISN_T_IT));
			}
			
			return false;
		}
		
		int consumption = 0;
		int skillid = 0;
		
		for (Summon pet : player.getSummonList())
		{
			switch (item.getId())
			{
				case 6645:
				case 20332:
					if (pet.getChargedSoulShot())
					{
						return false;
					}
					
					consumption = pet.getSoulshotConsumeCount();
					
					if (!player.getInventory().destroyItem(item, consumption))
					{
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DON_T_HAVE_ENOUGH_SOULSHOTS_NEEDED_FOR_A_PET_SERVITOR));
						return false;
					}
					
					pet.chargeSoulShot();
					skillid = 2033;
					break;
				
				case 6646:
				case 20333:
					if (pet.getChargedSpiritShot() > 0)
					{
						return false;
					}
					
					consumption = pet.getSpiritshotConsumeCount();
					
					if (!player.getInventory().destroyItem(item, consumption))
					{
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DON_T_HAVE_ENOUGH_SPIRITSHOTS_NEEDED_FOR_A_PET_SERVITOR));
						return false;
					}
					
					pet.chargeSpiritShot(ItemInstance.CHARGED_SPIRITSHOT);
					skillid = 2008;
					break;
				
				case 6647:
				case 20334:
					if (pet.getChargedSpiritShot() > 1)
					{
						return false;
					}
					
					consumption = pet.getSpiritshotConsumeCount();
					
					if (!player.getInventory().destroyItem(item, consumption))
					{
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DON_T_HAVE_ENOUGH_SPIRITSHOTS_NEEDED_FOR_A_PET_SERVITOR));
						return false;
					}
					
					pet.chargeSpiritShot(ItemInstance.CHARGED_BLESSED_SPIRITSHOT);
					skillid = 2009;
					break;
			}
			
			pet.broadcastPacket(new MagicSkillUse(pet, pet, skillid, 1, 0, 0));
		}
		
		return true;
	}
	
	/**
	 * Method getItemIds.
	 * @return int[]
	 * @see lineage2.gameserver.handlers.IItemHandler#getItemIds()
	 */
	@Override
	public final int[] getItemIds()
	{
		return _itemIds;
	}
}
