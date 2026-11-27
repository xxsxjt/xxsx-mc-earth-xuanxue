package com.xxsx.earth.xuanxue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An immutable spell graph: declared sources plus one typed pipeline.
 *
 * <p>Node costs are kept in two explicitly named units:
 * {@code costAeu} is field or stored charge drawn from an energy source, while
 * {@code costMana} is shared player mana paid at commit. They are never added
 * together or silently substituted for one another.</p>
 */
public record SpellGraph(String id, List<SpellSource> sources, List<Node> nodes) {
    public SpellGraph {
        sources = List.copyOf(sources);
        nodes = List.copyOf(nodes);
    }

    public Validation validate() {
        if (id == null || !id.matches("[a-z0-9_.]+:[a-z0-9_./]+")) {
            return Validation.invalid("bad_graph_id", "graph id must be namespaced lowercase");
        }
        if (sources.isEmpty()) {
            return Validation.invalid("missing_source", "graph must declare at least one source");
        }
        Set<SpellSource> uniqueSources = new HashSet<>();
        for (SpellSource source : sources) {
            if (!uniqueSources.add(source)) {
                return Validation.invalid("duplicate_source", "source " + source + " is declared twice");
            }
        }
        if (nodes.isEmpty()) {
            return Validation.invalid("empty_graph", "graph has no nodes");
        }
        Node first = nodes.get(0);
        if (!first.kind().acceptsInput(first.inputPort())) {
            return Validation.invalid("invalid_input_port",
                    "node 0 kind " + first.kind() + " cannot accept " + first.inputPort());
        }
        boolean sourceFeedsFirst = sources.stream()
                .anyMatch(source -> source.feeds(first.inputPort()));
        if (!sourceFeedsFirst) {
            return Validation.invalid("missing_source_edge",
                    "no declared source emits the first node port " + first.inputPort());
        }
        if (!nodes.get(nodes.size() - 1).kind().isSink()) {
            return Validation.invalid("missing_sink", "graph must end in an explicit sink");
        }
        boolean hasEffect = false;
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (!Double.isFinite(node.costAeu()) || node.costAeu() < 0.0D) {
                return Validation.invalid("bad_cost_aeu", "node " + i + " has an invalid AEU cost");
            }
            if (!Double.isFinite(node.costMana()) || node.costMana() < 0.0D) {
                return Validation.invalid("bad_cost_mana", "node " + i + " has an invalid MP cost");
            }
            if (node.kind().isEffect()) {
                hasEffect = true;
            }
            if (i > 0) {
                String previous = nodes.get(i - 1).kind().outputPort();
                if (!node.kind().acceptsInput(node.inputPort())) {
                    return Validation.invalid("invalid_input_port",
                            "node " + i + " kind " + node.kind() + " cannot accept " + node.inputPort());
                }
                if (!node.inputPort().equals(previous)) {
                    return Validation.invalid("port_mismatch",
                            "node " + i + " expects " + node.inputPort() + " but previous emits " + previous);
                }
            }
        }
        if (!hasEffect) {
            return Validation.invalid("missing_effect", "graph has no effect node");
        }
        if (energyCostAeu() + manaCost() <= 0.0D) {
            return Validation.invalid("free_spell", "graph must demand AEU or MP");
        }
        return Validation.VALID;
    }

    public double energyCostAeu() {
        return nodes.stream().mapToDouble(Node::costAeu).sum();
    }

    public double manaCost() {
        return nodes.stream().mapToDouble(Node::costMana).sum();
    }

    public boolean requires(SpellSource source) {
        return sources.contains(source);
    }

    public record Node(SpellNode kind, String inputPort, double costAeu, double costMana) {
        public Node {
            if (kind == null || inputPort == null || inputPort.isBlank()) {
                throw new IllegalArgumentException("spell node requires kind and input port");
            }
            if (!Double.isFinite(costAeu) || costAeu < 0.0D) {
                throw new IllegalArgumentException("spell node AEU cost must be finite and non-negative");
            }
            if (!Double.isFinite(costMana) || costMana < 0.0D) {
                throw new IllegalArgumentException("spell node MP cost must be finite and non-negative");
            }
        }
    }

    public record Validation(boolean valid, String errorCode, String message) {
        public static final Validation VALID = new Validation(true, "", "");

        public static Validation invalid(String errorCode, String message) {
            return new Validation(false, errorCode, message);
        }
    }
}
