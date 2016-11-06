package com.github.gdefacci.briscola.presentation.competition

import com.github.gdefacci.briscola.web.util.ArgonautHelper._
import argonaut._
import Argonaut._
import EncodeJson.derive

object CompetitionJsonEncoders {

  import com.github.gdefacci.briscola.presentation.CommonJsonEncoders._
  
  implicit lazy val competitionStateKindEncoder = enumEncoder[CompetitionStateKind.type] 
  
  implicit lazy val matchKindEncoder = {
  
    implicit lazy val matchKindKindEncoder = enumEncoder[MatchKindKind.type] 
    
    lazy val singleMatch = singletonADTEncoder[MatchKindKind.type, SingleMatch.type]
    lazy val numberOfGamesMatchKindEncoder = withKind[MatchKindKind.type](derive[NumberOfGamesMatchKind])
    lazy val targetPointsMatchKindEncoder = withKind[MatchKindKind.type](derive[TargetPointsMatchKind]) 
    
    jencode1((p: MatchKind) => p match {
      case t @ SingleMatch => singleMatch(t)
      case t:NumberOfGamesMatchKind => numberOfGamesMatchKindEncoder(t)
      case t:TargetPointsMatchKind => targetPointsMatchKindEncoder(t)
    })
  }
  
  implicit lazy val competitionStartDeadlineEncoder = {
    
    implicit lazy val competitionStartDeadlineKindEncoder = enumEncoder[CompetitionStartDeadlineKind.type]
    
    lazy val allPlayers = singletonADTEncoder[CompetitionStartDeadlineKind.type, AllPlayers.type] 
    lazy val onPlayerCountEncoder = withKind[CompetitionStartDeadlineKind.type](derive[OnPlayerCount]) 
    
    jencode1((p: CompetitionStartDeadline) => p match {
      case t @ AllPlayers => allPlayers(t)
      case t:OnPlayerCount => onPlayerCountEncoder(t)
    })
  }
      
  implicit lazy val competitionEncoder = EncodeJson.derive[Competition]
  
  implicit lazy val competitionStateEncoder = EncodeJson.derive[CompetitionState]

  implicit lazy val competitionEventEncoder = {
    
    implicit lazy val competitionEventKindEncoder = enumEncoder[CompetitionEventKind.type]
    
    lazy val createdCompetitionEncoder = withKind[CompetitionEventKind.type](derive[CreatedCompetition]) 
    lazy val competitionAcceptedEncoder = withKind[CompetitionEventKind.type](derive[CompetitionAccepted])
    lazy val competitionDeclinedEncoder = withKind[CompetitionEventKind.type](derive[CompetitionDeclined]) 

     jencode1((p: CompetitionEvent) => p match {
      case c:CreatedCompetition => createdCompetitionEncoder(c)
      case c:CompetitionAccepted => competitionAcceptedEncoder(c)
      case c:CompetitionDeclined => competitionDeclinedEncoder(c)
    })    
    
  } 
  
}