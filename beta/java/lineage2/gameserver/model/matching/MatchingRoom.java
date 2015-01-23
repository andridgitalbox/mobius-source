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
package lineage2.gameserver.model.matching;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import lineage2.gameserver.instancemanager.MatchingRoomManager;
import lineage2.gameserver.listener.actor.player.OnPlayerPartyInviteListener;
import lineage2.gameserver.listener.actor.player.OnPlayerPartyLeaveListener;
import lineage2.gameserver.model.Player;
import lineage2.gameserver.model.PlayerGroup;
import lineage2.gameserver.network.serverpackets.L2GameServerPacket;
import lineage2.gameserver.network.serverpackets.SystemMessage;
import lineage2.gameserver.network.serverpackets.components.IStaticPacket;

/**
 * @author Mobius
 * @version $Revision: 1.0 $
 */
public abstract class MatchingRoom implements PlayerGroup
{
	/**
	 * @author Mobius
	 */
	private class PartyListenerImpl implements OnPlayerPartyInviteListener, OnPlayerPartyLeaveListener
	{
		/**
		 * Constructor for PartyListenerImpl.
		 */
		public PartyListenerImpl()
		{
		}
		
		/**
		 * Method onPartyInvite.
		 * @param player Player
		 * @see lineage2.gameserver.listener.actor.player.OnPlayerPartyInviteListener#onPartyInvite(Player)
		 */
		@Override
		public void onPartyInvite(Player player)
		{
			broadcastPlayerUpdate(player);
		}
		
		/**
		 * Method onPartyLeave.
		 * @param player Player
		 * @see lineage2.gameserver.listener.actor.player.OnPlayerPartyLeaveListener#onPartyLeave(Player)
		 */
		@Override
		public void onPartyLeave(Player player)
		{
			broadcastPlayerUpdate(player);
		}
	}
	
	public static final int PARTY_MATCHING = 0;
	public static final int CC_MATCHING = 1;
	public static final int WAIT_PLAYER = 0;
	public static final int ROOM_MASTER = 1;
	public static final int PARTY_MEMBER = 2;
	public static final int UNION_LEADER = 3;
	public static final int UNION_PARTY = 4;
	public static final int WAIT_PARTY = 5;
	public static final int WAIT_NORMAL = 6;
	private final int _id;
	private int _minLevel;
	private int _maxLevel;
	private int _maxMemberSize;
	private int _lootType;
	private String _topic;
	private final PartyListenerImpl _listener = new PartyListenerImpl();
	protected final Player _leader;
	private final Set<Player> _members = new CopyOnWriteArraySet<>();
	
	/**
	 * Constructor for MatchingRoom.
	 * @param leader Player
	 * @param minLevel int
	 * @param maxLevel int
	 * @param maxMemberSize int
	 * @param lootType int
	 * @param topic String
	 */
	MatchingRoom(Player leader, int minLevel, int maxLevel, int maxMemberSize, int lootType, String topic)
	{
		_leader = leader;
		_id = MatchingRoomManager.getInstance().addMatchingRoom(this);
		_minLevel = minLevel;
		_maxLevel = maxLevel;
		_maxMemberSize = maxMemberSize;
		_lootType = lootType;
		_topic = topic;
		addMember0(leader, null);
	}
	
	// ===============================================================================================================================================
	// Add/Remove Member
	// ===============================================================================================================================================
	/**
	 * Method addMember.
	 * @param player Player
	 * @return boolean
	 */
	public boolean addMember(Player player)
	{
		if (_members.contains(player))
		{
			return true;
		}
		
		if ((player.getLevel() < getMinLevel()) || (player.getLevel() > getMaxLevel()) || (getPlayers().size() >= getMaxMembersSize()))
		{
			player.sendPacket(notValidMessage());
			return false;
		}
		
		return addMember0(player, enterMessage().addPcName(player));
	}
	
	/**
	 * Method addMember0.
	 * @param player Player
	 * @param p L2GameServerPacket
	 * @return boolean
	 */
	private boolean addMember0(Player player, L2GameServerPacket p)
	{
		if (!_members.isEmpty())
		{
			player.addListener(_listener);
		}
		
		_members.add(player);
		player.setMatchingRoom(this);
		
		for (Player $member : this)
		{
			if ($member != player)
			{
				$member.sendPacket(p, addMemberPacket($member, player));
			}
		}
		
		MatchingRoomManager.getInstance().removeFromWaitingList(player);
		player.sendPacket(infoRoomPacket(), membersPacket(player));
		player.sendChanges();
		return true;
	}
	
	/**
	 * Method removeMember.
	 * @param member Player
	 * @param oust boolean
	 */
	public void removeMember(Player member, boolean oust)
	{
		if (!_members.remove(member))
		{
			return;
		}
		
		member.removeListener(_listener);
		member.setMatchingRoom(null);
		
		if (_members.isEmpty())
		{
			disband();
		}
		else
		{
			L2GameServerPacket infoPacket = infoRoomPacket();
			SystemMessage exitMessage0 = exitMessage(true, oust);
			L2GameServerPacket exitMessage = exitMessage0 != null ? exitMessage0.addPcName(member) : null;
			
			for (Player player : this)
			{
				player.sendPacket(infoPacket, removeMemberPacket(player, member), exitMessage);
			}
		}
		
		member.sendPacket(closeRoomPacket(), exitMessage(false, oust));
		MatchingRoomManager.getInstance().addToWaitingList(member);
		member.sendChanges();
	}
	
	/**
	 * Method broadcastPlayerUpdate.
	 * @param player Player
	 */
	public void broadcastPlayerUpdate(Player player)
	{
		for (Player $member : MatchingRoom.this)
		{
			$member.sendPacket(updateMemberPacket($member, player));
		}
	}
	
	/**
	 * Method disband.
	 */
	public void disband()
	{
		for (Player player : this)
		{
			player.removeListener(_listener);
			player.sendPacket(closeRoomMessage());
			player.sendPacket(closeRoomPacket());
			player.setMatchingRoom(null);
			player.sendChanges();
			MatchingRoomManager.getInstance().addToWaitingList(player);
		}
		
		_members.clear();
		MatchingRoomManager.getInstance().removeMatchingRoom(this);
	}
	
	// ===============================================================================================================================================
	// Abstracts
	// ===============================================================================================================================================
	/**
	 * Method notValidMessage.
	 * @return SystemMessage
	 */
	public abstract SystemMessage notValidMessage();
	
	/**
	 * Method enterMessage.
	 * @return SystemMessage
	 */
	public abstract SystemMessage enterMessage();
	
	/**
	 * Method exitMessage.
	 * @param toOthers boolean
	 * @param kick boolean
	 * @return SystemMessage
	 */
	public abstract SystemMessage exitMessage(boolean toOthers, boolean kick);
	
	/**
	 * Method closeRoomMessage.
	 * @return SystemMessage
	 */
	public abstract SystemMessage closeRoomMessage();
	
	/**
	 * Method closeRoomPacket.
	 * @return L2GameServerPacket
	 */
	public abstract L2GameServerPacket closeRoomPacket();
	
	/**
	 * Method infoRoomPacket.
	 * @return L2GameServerPacket
	 */
	public abstract L2GameServerPacket infoRoomPacket();
	
	/**
	 * Method addMemberPacket.
	 * @param $member Player
	 * @param active Player
	 * @return L2GameServerPacket
	 */
	public abstract L2GameServerPacket addMemberPacket(Player $member, Player active);
	
	/**
	 * Method removeMemberPacket.
	 * @param $member Player
	 * @param active Player
	 * @return L2GameServerPacket
	 */
	public abstract L2GameServerPacket removeMemberPacket(Player $member, Player active);
	
	/**
	 * Method updateMemberPacket.
	 * @param $member Player
	 * @param active Player
	 * @return L2GameServerPacket
	 */
	public abstract L2GameServerPacket updateMemberPacket(Player $member, Player active);
	
	/**
	 * Method membersPacket.
	 * @param active Player
	 * @return L2GameServerPacket
	 */
	public abstract L2GameServerPacket membersPacket(Player active);
	
	/**
	 * Method getType.
	 * @return int
	 */
	public abstract int getType();
	
	/**
	 * Method getMemberType.
	 * @param member Player
	 * @return int
	 */
	public abstract int getMemberType(Player member);
	
	// ===============================================================================================================================================
	// Broadcast
	// ===============================================================================================================================================
	/**
	 * Method broadCast.
	 * @param arg IStaticPacket[]
	 * @see lineage2.gameserver.model.PlayerGroup#broadCast(IStaticPacket[])
	 */
	@Override
	public void broadCast(IStaticPacket... arg)
	{
		for (Player player : this)
		{
			player.sendPacket(arg);
		}
	}
	
	// ===============================================================================================================================================
	// Getters
	// ===============================================================================================================================================
	/**
	 * Method getId.
	 * @return int
	 */
	public int getId()
	{
		return _id;
	}
	
	/**
	 * Method getMinLevel.
	 * @return int
	 */
	public int getMinLevel()
	{
		return _minLevel;
	}
	
	/**
	 * Method getMaxLevel.
	 * @return int
	 */
	public int getMaxLevel()
	{
		return _maxLevel;
	}
	
	/**
	 * Method getTopic.
	 * @return String
	 */
	public String getTopic()
	{
		return _topic;
	}
	
	/**
	 * Method getMaxMembersSize.
	 * @return int
	 */
	public int getMaxMembersSize()
	{
		return _maxMemberSize;
	}
	
	/**
	 * Method getLocationId.
	 * @return int
	 */
	public int getLocationId()
	{
		return MatchingRoomManager.getInstance().getLocation(_leader);
	}
	
	/**
	 * Method getLeader.
	 * @return Player
	 */
	public Player getLeader()
	{
		return _leader;
	}
	
	/**
	 * Method getPlayers.
	 * @return Collection<Player>
	 */
	public Collection<Player> getPlayers()
	{
		return _members;
	}
	
	/**
	 * Method getLootType.
	 * @return int
	 */
	public int getLootType()
	{
		return _lootType;
	}
	
	/**
	 * Method iterator.
	 * @return Iterator<Player> * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Player> iterator()
	{
		return _members.iterator();
	}
	
	// ===============================================================================================================================================
	// Setters
	// ===============================================================================================================================================
	/**
	 * Method setMinLevel.
	 * @param minLevel int
	 */
	public void setMinLevel(int minLevel)
	{
		_minLevel = minLevel;
	}
	
	/**
	 * Method setMaxLevel.
	 * @param maxLevel int
	 */
	public void setMaxLevel(int maxLevel)
	{
		_maxLevel = maxLevel;
	}
	
	/**
	 * Method setTopic.
	 * @param topic String
	 */
	public void setTopic(String topic)
	{
		_topic = topic;
	}
	
	/**
	 * Method setMaxMemberSize.
	 * @param maxMemberSize int
	 */
	public void setMaxMemberSize(int maxMemberSize)
	{
		_maxMemberSize = maxMemberSize;
	}
	
	/**
	 * Method setLootType.
	 * @param lootType int
	 */
	public void setLootType(int lootType)
	{
		_lootType = lootType;
	}
}