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
package lineage2.gameserver.stats.conditions;

import lineage2.gameserver.model.Creature;
import lineage2.gameserver.model.Effect;
import lineage2.gameserver.skills.EffectType;
import lineage2.gameserver.stats.Env;

public final class ConditionTargetHasBuff extends Condition
{
	private final EffectType _effectType;
	private final int _level;
	
	public ConditionTargetHasBuff(EffectType effectType, int level)
	{
		_effectType = effectType;
		_level = level;
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		Creature target = env.target;
		if (target == null)
		{
			return false;
		}
		Effect effect = target.getEffectList().getEffectByType(_effectType);
		if (effect == null)
		{
			return false;
		}
		if ((_level == -1) || (effect.getSkill().getLevel() >= _level))
		{
			return true;
		}
		return false;
	}
}