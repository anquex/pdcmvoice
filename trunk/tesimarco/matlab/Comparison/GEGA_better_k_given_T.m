% vedi file test3 per biased/unbiased
close all
hold on
hold all

Ts=[0:4 5:5:125];
%Ts=[0:128]
batchsizes=[10 100 1000];
theta=0.99;
ii=1;
for n=batchsizes
    K=zeros(1,length(Ts));
    lD=0;
    numbers=[];
    i=1;
    for T=Ts
        T
        load(['D-n-' int2str(n) '-T-' int2str(T)],'D');
        if(lD~=length(D))
            numbers=1:length(D);
        end
        k=find_min_k_given_T_and_theta(n,D,theta);
        K(i)=k;
        i=i+1;
    end
    h{ii}=plot(Ts,K);
    ii=ii+1;
end


%%%%%%%%%%%%%% PLOT   %%%%%%%%%%%%%%%

legend('$n=10$','$n=10^2$','$n=10^3$');
hl = legend;
set(hl, 'Interpreter', 'Latex');
set(hl, 'FontSize', 11);
set(hl, 'Location', 'Best');

grid on;

hx=xlabel('$T$','Interpreter','Latex');
hy=ylabel('$k$','Interpreter','Latex');
set(hx, 'FontSize', 11);
set(hy, 'FontSize', 11);

set(gca,'XLim',[0 125]);
%set(gca,'XTick',0:nmax);
%set(gca,'YTick',0:5:50);

fh = figure(1); % returns the handle to the figure object
set(fh, 'color', 'white'); % sets the color to white
set(gca,'Box','on');

set(h{1}, 'LineStyle', '-', 'LineWidth', 1.0);
%set(h{1}, 'Marker', 'o', 'MarkerFaceColor', [0.5 0.5 0.5], 'MarkerEdgeColor', [0 0 0], 'MarkerSize', 4.0);
set(h{2}, 'LineStyle', '-', 'LineWidth', 1.0);
set(h{2}, 'Marker', 's', 'MarkerFaceColor', [1 1 1], 'MarkerEdgeColor', [0 0 0], 'MarkerSize', 4.0);
set(h{3}, 'LineStyle', '-', 'LineWidth', 1.0);
set(h{3}, 'Marker', 'd', 'MarkerFaceColor', [.8 .8 .8], 'MarkerEdgeColor', [0 0 0], 'MarkerSize', 5.0);

title('Minimum $T$ to achieve accuracy $k$','interpreter','Latex');
% axis ([-0 19 0 55]);
% 
% h=findobj(gcf,'type','axes','tag','legend');
% Pos=get(h,'position')
% Pos(3)=1.05*Pos(3); % Double the length
% set(h,'position',Pos) % Implement it

%saveas(fh, 'GEGA-min-T-for-k', 'epsc');
return;

set(gca,'YLim',[1.2 2.25])
set(gca,'XTick',[0:5:120])
set(gca,'YTick',[1:0.1:2.2])
title('Minimum $T$ to achieve accuracy $k$: Detailed View','interpreter','Latex');

%saveas(fh, 'GEGA-min-T-for-k-detail', 'epsc');