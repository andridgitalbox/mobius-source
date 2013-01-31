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
package ai;

import lineage2.gameserver.ai.Fighter;
import lineage2.gameserver.model.Creature;
import lineage2.gameserver.model.entity.Reflection;
import lineage2.gameserver.model.instances.NpcInstance;

/**
 * @author Mobius
 * @version $Revision: 1.0 $
 */
public class Aenkinel extends Fighter
{
	/**
	 * Constructor for Aenkinel.
	 * @param actor NpcInstance
	 */
	public Aenkinel(NpcInstance actor)
	{
		super(actor);
	}
	
	/**
	 * Method onEvtDead.
	 * @param killer Creature
	 */
	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();
		if ((actor.getNpcId() == 25694) || (actor.getNpcId() == 25695))
		{
			Reflection ref = actor.getReflection();
			ref.setReenterTime(System.currentTimeMillis());
		}
		if (actor.getNpcId() == 25694)
		{
			for (int i = 0; i < 4; i++)
			{
				actor.getReflection().addSpawnWithoutRespawn(18820, actor.getLoc(), 250);
			}
		}
		else if (actor.getNpcId() == 25695)
		{
			for (int i = 0; i < 4; i++)
			{
				actor.getReflection().addSpawnWithoutRespawn(18823, actor.getLoc(), 250);
			}
		}
		super.onEvtDead(killer);
	}
}
