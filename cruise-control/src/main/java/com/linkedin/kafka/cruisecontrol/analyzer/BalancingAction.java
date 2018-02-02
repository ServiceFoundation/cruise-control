/*
 * Copyright 2017 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License"). See License in the project root for license information.
 */

package com.linkedin.kafka.cruisecontrol.analyzer;

import com.linkedin.kafka.cruisecontrol.common.ActionType;
import org.apache.kafka.common.TopicPartition;
import java.util.HashMap;
import java.util.Map;


/**
 * Represents the load balancing operation over a replica for Kafka Load GoalOptimizer.
 */
public class BalancingAction {
  private final TopicPartition _tp;
  private final Integer _sourceBrokerId;
  private final Integer _destinationBrokerId;
  private final ActionType _actionType;
  private final long _dataToMove;

  /**
   * Constructor for creating a balancing proposal with given topic partition, source broker and destination id, and
   * balancing action.
   *
   * @param tp                  Topic partition of the replica.
   * @param sourceBrokerId      Source broker id of the replica.
   * @param destinationBrokerId Destination broker id of the replica.
   * @param actionType     Leadership transfer or replica relocation.
   */
  public BalancingAction(TopicPartition tp, Integer sourceBrokerId, Integer destinationBrokerId,
                         ActionType actionType) {
    this(tp, sourceBrokerId, destinationBrokerId, actionType, 0L);
  }

  /**
   * Constructor for creating a balancing proposal with given topic partition, source broker and destination id, and
   * balancing action.
   *
   * @param tp                  Topic partition of the replica.
   * @param sourceBrokerId      Source broker id of the replica.
   * @param destinationBrokerId Destination broker id of the replica.
   * @param actionType     Leadership transfer or replica relocation.
   * @param dataToMove          The data to move with this proposal. The unit should be MB.
   */
  public BalancingAction(TopicPartition tp,
                         Integer sourceBrokerId,
                         Integer destinationBrokerId,
                         ActionType actionType,
                         long dataToMove) {
    _tp = tp;
    _sourceBrokerId = sourceBrokerId;
    _destinationBrokerId = destinationBrokerId;
    _actionType = actionType;
    _dataToMove = dataToMove;
    validate();
  }
  
  private void validate() {
    switch (_actionType) {
      case REPLICA_ADDITION:
        if (_destinationBrokerId == null) {
          throw new IllegalArgumentException("The destination broker cannot be null for balancing action " + _actionType);
        } else if (_sourceBrokerId != null) {
          throw new IllegalArgumentException("The source broker should be null for balancing action " + _actionType);
        }
        break;
      case REPLICA_DELETION:
        if (_destinationBrokerId != null) {
          throw new IllegalArgumentException("The destination broker should be null for balancing action " + _actionType);
        } else if (_sourceBrokerId == null) {
          throw new IllegalArgumentException("The source broker cannot be null for balancing action " + _actionType);
        }
        break;
      case REPLICA_MOVEMENT:
      case LEADERSHIP_MOVEMENT:
        if (_destinationBrokerId == null) {
          throw new IllegalArgumentException("The destination broker cannot be null for balancing action " + _actionType);
        } else if (_sourceBrokerId == null) {
          throw new IllegalArgumentException("The source broker cannot be null for balancing action " + _actionType);
        }
        break;
      default:
        throw new IllegalStateException("should never be here");
    }
  }

  /**
   * Get the partition Id that is impacted by the balancing action.
   */
  public int partitionId() {
    return _tp.partition();
  }

  /**
   * Get topic name of the impacted partition.
   */
  public String topic() {
    return _tp.topic();
  }

  /**
   * Get topic partition.
   */
  public TopicPartition topicPartition() {
    return _tp;
  }

  /**
   * Get the source broker id that is impacted by the balancing action.
   */
  public Integer sourceBrokerId() {
    return _sourceBrokerId;
  }

  /**
   * Get the destination broker Id.
   */
  public Integer destinationBrokerId() {
    return _destinationBrokerId;
  }

  /**
   * Get the type of action that provides balancing.
   */
  public ActionType balancingAction() {
    return _actionType;
  }

  /**
   * Get the data to move with the proposal. The unit should be MB.
   */
  public long dataToMove() {
    return _dataToMove;
  }

  /*
   * Return an object that can be further used
   * to encode into JSON
   */
  public Map<String, Object> getJsonStructure() {
    Map<String, Object> proposalMap = new HashMap<>();
    proposalMap.put("topicPartition", _tp);
    proposalMap.put("sourceBrokerId", _sourceBrokerId);
    proposalMap.put("destinationBrokerId", _destinationBrokerId);
    proposalMap.put("balancingAction", _actionType);
    return proposalMap;
  }

  /**
   * Get string representation of this balancing proposal.
   */
  @Override
  public String toString() {
    return String.format("(%s, %d->%d, %s)", _tp, _sourceBrokerId, _destinationBrokerId, _actionType);
  }

  /**
   * Compare the given object with this object.
   *
   * @param other Other object to be compared with this object.
   * @return True if other object equals this object, false otherwise.
   */
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof BalancingAction)) {
      return false;
    }

    if (this == other) {
      return true;
    }
    
    BalancingAction otherProposal = (BalancingAction) other;
    if (_sourceBrokerId == null) {
      if (otherProposal._sourceBrokerId != null) {
        return false;
      }
    } else if (!_sourceBrokerId.equals(otherProposal._sourceBrokerId)) {
      return false;
    }

    if (_destinationBrokerId == null) {
      if (otherProposal._destinationBrokerId != null) {
        return false;
      }
    } else if (!_destinationBrokerId.equals(otherProposal._destinationBrokerId)) {
      return false;
    }
    
    return _tp.equals(otherProposal._tp) && _actionType == otherProposal._actionType;
  }

  @Override
  public int hashCode() {
    int result = _tp.hashCode();
    result = 31 * result + _sourceBrokerId;
    result = 31 * result + _destinationBrokerId;
    result = 31 * result + _actionType.hashCode();
    return result;
  }
}