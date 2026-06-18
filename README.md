# Java Bossfight

Base inicial de um jogo 2D de boss fight em Java 21 com LibGDX e LWJGL3.

## Como executar

1. Instale um JDK 21.
2. Execute:

```powershell
.\gradlew.bat run
```

## Controles

- `A` / `Seta esquerda`: mover para a esquerda
- `D` / `Seta direita`: mover para a direita
- `W` / `Espaco` / `Seta cima`: pular
- `J` / `Ctrl`: atirar
- `K` / `Shift`: dash
- `L` / `Alt`: ataque especial
- `Esc`: voltar ao menu

## Destaques atuais

- Menu inicial com navegacao, feedback visual e sons procedurais.
- Sequencia de entrada da luta com boss inofensivo, revelacao agressiva, `READY?` e `GO!`.
- Player com dash com particulas, invencibilidade temporaria, knockback, hitstop e ataque especial carregavel.
- Boss floral procedural com maquina de estados, telegraphs visuais, reacao a dano e multiplos ataques.
- UI de vida do player, vida do boss e barra de especial.
- Sem assets oficiais de Cuphead; a arte e o audio atuais sao placeholders procedurais.

## Organizacao

- `screens`: telas do jogo.
- `entities`: objetos do jogo, como jogador, boss, projeteis e hitboxes.
- `boss`: estados da maquina de estados do boss.
- `systems`: sistemas simples de colisao, projeteis, animacao e audio.
- `util`: constantes globais e wrapper de assets.
- `assets`: sprites, sons e fontes futuros.
