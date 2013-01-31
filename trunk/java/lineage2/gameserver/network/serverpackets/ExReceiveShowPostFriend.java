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
package lineage2.gameserver.network.serverpackets;

import lineage2.gameserver.model.Player;

import org.napile.primitive.maps.IntObjectMap;

/**
 * @author Mobius
 * @version $Revision: 1.0 $
 */
public class ExReceiveShowPostFriend extends L2GameServerPacket
{
	/**
	 * Field _list.
	 */
	private final IntObjectMap<String> _list;
	
	/**
	 * Constructor for ExReceiveShowPostFriend.
	 * @param player Player
	 */
	public ExReceiveShowPostFriend(Player player)
	{
		_list = player.getPostFriends();
	}
	
	/**
	 * Method writeImpl.
	 */
	@Override
	public void writeImpl()
	{
		writeEx(0xD3);
		writeD(_list.size());
		for (String t : _list.values())
		{
			writeS(t);
		}
	}
}
