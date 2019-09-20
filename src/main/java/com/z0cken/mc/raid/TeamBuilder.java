package com.z0cken.mc.raid;

import com.z0cken.mc.core.FriendsAPI;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class TeamBuilder {

    private final Set<UUID> players;
    private final Queue<UUID> queue;

    private final boolean groupFriends;
    private final int maxImbalance;

    public TeamBuilder(Set<UUID> players, boolean groupFriends, int maxImbalance) {
        this.players = Collections.unmodifiableSet(players);
        this.queue = new LinkedList<>(players);
        this.groupFriends = groupFriends;
        this.maxImbalance = maxImbalance;
    }

    public List<UUID>[] build() {
        if(!groupFriends) {
            return Util.splitHalf(new ArrayList<>(players));
        }

        List<UUID> a = new ArrayList<>(), b = new ArrayList<>();
        List<UUID>[] r = new List[] { a, b };

        for(Group group : findGroups()) {
            if(a.size() < b.size()) {
                a.addAll(group.groupPlayers);
            } else {
                b.addAll(group.groupPlayers);
            }
        }

        //If negative, b > a
        final int difference = a.size() - b.size();
        if(Math.abs(difference) > maxImbalance) {
           for(int i = 0; i < Math.abs(difference); i++) {
               if(difference < 0) {
                   int random = new Random().nextInt(b.size());
                   UUID uuid = b.get(random);
                   b.remove(random);
                   a.add(uuid);
               } else {
                   int random = new Random().nextInt(a.size());
                   UUID uuid = a.get(random);
                   a.remove(random);
                   b.add(uuid);
               }
           }
        }

        return r;
    }

    private Collection<Group> findGroups() {
        if(queue.isEmpty()) throw new IllegalStateException();

        Collection<Group> groups = new ArrayList<>();

        while (!queue.isEmpty()) {
            final UUID uuid = queue.poll();

            Set<UUID> set = new HashSet<>();
            set.add(uuid);
            Group g = new Group(set);
            groups.add(g);
            computeGroup(g, uuid);
        }

        return groups;
    }

    private void computeGroup(Group group, UUID uuid) {
        final Collection<UUID> friends;
        try {
            friends = getFriends(uuid);
            friends.removeAll(group.groupPlayers);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        group.groupPlayers.addAll(friends);
        queue.removeAll(friends);

        for(UUID u : friends) computeGroup(group, u);
    }

    private Collection<UUID> getFriends(UUID uuid) throws SQLException {
        return FriendsAPI.getFriends(uuid).keySet().stream().filter(players::contains).collect(Collectors.toSet());
    }

    public static class Group implements Comparable<Group> {
        private final Collection<UUID> groupPlayers;

        public Group(Collection<UUID> groupPlayers) {
            this.groupPlayers = groupPlayers;
        }

        public int getSize() {
            return groupPlayers.size();
        }

        @Override
        public int compareTo(Group o) {
            return Integer.compare(getSize(), o.getSize());
        }
    }

}
