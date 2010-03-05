%andamento varianza
f1=inline('(1-x)./x')
x=[0:0.01:1];
h1=plot(x,f1(x),'red');
hy=ylabel('$$var(\hat{n}|n)/n$$','Interpreter','latex');
hx=xlabel('$$p_\epsilon$$','Interpreter','latex');

legend('$(1-p_\epsilon)/p_\epsilon$');
hl = legend;
set(hl, 'interpreter', 'latex');
set(gca,'FontSize',10)

set(hl, 'FontSize', 15);
set(hx, 'FontSize', 15);
set(hy, 'FontSize', 15);
grid on;
 set(gca, 'Box', 'off' ); % here gca means get current axis
 set(gca, 'TickDir', 'out', 'XTick', [0:0.1:1]);
 fh=figure(1);
 set(fh, 'color', 'white'); % sets the color to white
 set(h1, 'LineStyle', '-', 'LineWidth', 1.0, 'Color', 'Black');
 set(h1, 'Marker', 'o', 'MarkerFaceColor', [0.5 0.5 0.5], 'MarkerEdgeColor', [0 0 0], 'MarkerSize', 4.0);
 
 saveas(fh, 'cidon-variance-p', 'epsc');
 %saveas(fh, 'cidon-variance-p', 'jpg');
%set(gca,'XTick',-pi:pi/2:pi)
%set(gca,'XTickLabel',[0:0.1:1])
% TRICK FOR LATEX IN LEGEND
 % Use legend text that's about the width of the final result.
%  h = legend('a');

  % Find the first text object (alpha) and change it.
 % h1 = findobj(get(h,'Children'),'String','a');
  %set(h1,'String','$$(1-p_\epsilon)/p_\epsilon$$','Interpreter','latex')