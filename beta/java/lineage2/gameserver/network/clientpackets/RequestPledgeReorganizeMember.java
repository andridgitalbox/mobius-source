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
package lineage2.gameserver.network.clientpackets;

import lineage2.gameserver.model.Player;
import lineage2.gameserver.model.pledge.Clan;
import lineage2.gameserver.model.pledge.SubUnit;
import lineage2.gameserver.model.pledge.UnitMember;
import lineage2.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import lineage2.gameserver.network.serverpackets.SystemMessage;
import lineage2.gameserver.network.serverpackets.components.SystemMessageId;

/**
 * @author Mobius
 * @version $Revision: 1.0 $
 */
public class RequestPledgeReorganizeMember extends L2GameClientPacket
{
	private int _replace;
	private String _subjectName;
	private int _targetUnit;
	private String _replaceName;
	
	/**
	 * Method readImpl.
	 */
	@Override
	protected void readImpl()
	{
		_replace = readD();
		_subjectName = readS(16);
		_targetUnit = readD();
		
		if (_replace > 0)
		{
			_replaceName = readS();
		}
	}
	
	/**
	 * Method runImpl.
	 */
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		Clan clan = activeChar.getClan();
		
		if (clan == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		
		if (!activeChar.isClanLeader())
		{
			activeChar.sendMessage("Only clan leader can change affiliations.");
			activeChar.sendActionFailed();
			return;
		}
		
		UnitMember subject = clan.getAnyMember(_subjectName);
		
		if (subject == null)
		{
			activeChar.sendMessage("The target doesn't belong to your clan.");
			activeChar.sendActionFailed();
			return;
		}
		
		if (subject.getPledgeType() == _targetUnit)
		{
			activeChar.sendMessage("The target is already in that combat unit.");
			activeChar.sendActionFailed();
			return;
		}
		
		if ((_targetUnit != 0) && (clan.getSubUnit(_targetUnit) == null))
		{
			activeChar.sendMessage("There is no such combat unit in your clan.");
			activeChar.sendActionFailed();
			return;
		}
		
		if (Clan.isAcademy(_targetUnit))
		{
			activeChar.sendMessage("The Academy can accept new members only via invitation.");
			activeChar.sendActionFailed();
			return;
		}
		
		if (Clan.isAcademy(subject.getPledgeType()))
		{
			activeChar.sendMessage("You can't move an Academy Member.");
			activeChar.sendActionFailed();
			return;
		}
		
		UnitMember replacement = null;
		
		if (_replace > 0)
		{
			replacement = clan.getAnyMember(_replaceName);
			
			if (replacement == null)
			{
				activeChar.sendMessage("The replacement character doesn't belong to your clan.");
				activeChar.sendActionFailed();
				return;
			}
			
			if (replacement.getPledgeType() != _targetUnit)
			{
				activeChar.sendMessage("The replacement character doesn't belong to combat unit given.");
				activeChar.sendActionFailed();
				return;
			}
			
			if (replacement.isSubLeader() != 0)
			{
				activeChar.sendMessage("The replacement is a leader of another unit and can't be moved.");
				activeChar.sendActionFailed();
				return;
			}
		}
		else
		{
			if (clan.getUnitMembersSize(_targetUnit) >= clan.getSubPledgeLimit(_targetUnit))
			{
				if (_targetUnit == Clan.SUBUNIT_MAIN_CLAN)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_FULL_AND_CANNOT_ACCEPT_ADDITIONAL_CLAN_MEMBERS_AT_THIS_TIME).addString(clan.getName()));
				}
				else
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_ACADEMY_ROYAL_GUARD_ORDER_OF_KNIGHTS_IS_FULL_AND_CANNOT_ACCEPT_NEW_MEMBERS_AT_THIS_TIME));
				}
				
				activeChar.sendActionFailed();
				return;
			}
			
			if (subject.isSubLeader() != 0)
			{
				activeChar.sendMessage("This member is a leader of another unit and can't be moved.");
				activeChar.sendActionFailed();
				return;
			}
		}
		
		SubUnit oldUnit = null;
		
		if (replacement != null)
		{
			oldUnit = replacement.getSubUnit();
			oldUnit.replace(replacement.getObjectId(), subject.getPledgeType());
			clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(replacement));
			
			if (replacement.isOnline())
			{
				replacement.getPlayer().updatePledgeClass();
				replacement.getPlayer().broadcastCharInfo();
			}
		}
		
		oldUnit = subject.getSubUnit();
		oldUnit.replace(subject.getObjectId(), _targetUnit);
		clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(subject));
		
		if (subject.isOnline())
		{
			subject.getPlayer().updatePledgeClass();
			subject.getPlayer().broadcastCharInfo();
		}
	}
}
