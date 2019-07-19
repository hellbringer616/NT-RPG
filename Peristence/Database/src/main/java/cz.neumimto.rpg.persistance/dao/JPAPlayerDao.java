/*
 *     Copyright (c) 2015, NeumimTo https://github.com/NeumimTo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package cz.neumimto.rpg.persistance.dao;

import cz.neumimto.rpg.api.persistance.model.CharacterBase;
import cz.neumimto.rpg.api.persistance.model.CharacterSkill;
import cz.neumimto.rpg.common.persistance.dao.IPlayerDao;
import cz.neumimto.rpg.persistance.model.JPACharacterBase;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by NeumimTo on 9.7.2015.
 */
//todo catch exceptions and rollback transactions
@Singleton
public class JPAPlayerDao extends GenericDao<CharacterBase> implements IPlayerDao {

    private SessionFactory factory;

    @Override
    public List<CharacterBase> getPlayersCharacters(UUID uuid) {
        Session session = getFactory().openSession();
        Query query = session.createQuery("SELECT a FROM JPACharacterBase a WHERE a.uuid=:id AND a.markedForRemoval = null ORDER BY a.updated DESC");
        query.setParameter("id", uuid);
        List list = query.list();
        session.close();
        return list;
    }


    public CharacterBase fetchCharacterBase(CharacterBase base) {
        Session session = getFactory().openSession();
        JPACharacterBase cb = (JPACharacterBase) session.merge(base);
        session.beginTransaction();
        cb.getCharacterSkills().size();
        cb.getCharacterClasses().size();
        cb.getBaseCharacterAttribute().size();
        cb.getCharacterSkills().size();
        session.getTransaction().commit();
        session.close();
        return cb;
    }

    @Override
    public CharacterBase getLastPlayed(UUID uuid) {
        Session session = getFactory().openSession();
        List r = session.createCriteria(JPACharacterBase.class)
                .add(Restrictions.eq("uuid", uuid.toString()))
                .addOrder(Order.desc("updated"))
                .add(Restrictions.ne("markedForRemoval", true))
                .list();
        session.close();

        if (r.size() == 0) {
            return null;
        }
        return (CharacterBase) r.get(0);
    }

    @Override
    public CharacterBase getCharacter(UUID player, String name) {
        Session s = getFactory().openSession();
        s.beginTransaction();
        Query query = s.createQuery("SELECT a FROM JPACharacterBase a WHERE a.uuid=:uuid and a.name=:name AND a.markedForRemoval = null");
        query.setParameter("uuid", player);
        query.setParameter("name", name);
        List<CharacterBase> list = query.list();
        s.close();
        if (list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public int getCharacterCount(UUID uuid) {
        Session s = getFactory().openSession();
        s.beginTransaction();
        Query query = s.createQuery("SELECT COUNT(c.id) FROM JPACharacterBase c WHERE c.uuid=:id AND a.markedForRemoval = null");
        query.setParameter("id", uuid);
        int i = query.getFirstResult();
        s.close();
        return i;
    }

    /**
     * @param uniqueId
     * @return rows updated
     */
    @Override
    public int deleteData(UUID uniqueId) {
        Session session = getFactory().openSession();
        Transaction transaction = session.beginTransaction();
        int i = -1;
        try {
            Query query = session.createQuery("DELETE FROM JPACharacterBase where uuid=:uuid");
            query.setParameter("uuid", uniqueId);
            i = query.executeUpdate();
            transaction.commit();
        } catch (Throwable t) {
            transaction.rollback();
        } finally {
            session.close();
        }
        return i;
    }

    @Override
    public void createAndUpdate(CharacterBase base) {
        Session session = getFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.save(base);
        session.flush();
        tx.commit();
        session.close();
    }

    @Override
    public int markCharacterForRemoval(UUID player, String charName) {
        String hql = "update JPACharacterBase b set b.markedForRemoval=:t where uuid= :uid AND lower(name)= :name";
        Session session = getFactory().openSession();
        Query query = session.createQuery(hql);
        Transaction transaction = session.beginTransaction();
        int updatecount;
        try {
            query.setParameter("uid", player);
            query.setParameter("name", charName.toLowerCase());
            query.setParameter("t", true);
            updatecount = query.executeUpdate();
            transaction.commit();
            return updatecount;
        } catch (Throwable t) {
            transaction.rollback();
            return 0;
        }

    }


    @Override
    public SessionFactory getFactory() {
        return this.factory;
    }

    public void removePeristantSkill(CharacterSkill characterSkill) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", characterSkill);
        String hql = "delete from CharacterSkill where skillId = :id";

        Session session = getFactory().openSession();
        Query query = session.createQuery(hql);
        Transaction transaction = session.beginTransaction();
        query.setParameter("id", characterSkill.getId());

        try {
            query.executeUpdate();
            transaction.commit();
        } catch (Throwable t) {
            transaction.rollback();
            throw new RuntimeException(t);
        }
    }
}